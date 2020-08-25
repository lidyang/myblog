package com.wintersun.controller;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wintersun.common.lang.Result;
import com.wintersun.config.RabbitConfig;
import com.wintersun.entity.*;
import com.wintersun.search.mq.PostMqIndexMessage;
import com.wintersun.util.PageInfoUtil;
import com.wintersun.util.ValidationUtil;
import com.wintersun.vo.CommentVo;
import com.wintersun.vo.PostVo;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;
import java.util.Map;


@Controller
public class PostController extends  BaseController{

    @RequestMapping("/category/{id:\\d}")
    public String category(@PathVariable(name="id") Long id){
        System.out.println("method: category " );

        int pn = ServletRequestUtils.getIntParameter(request, "pn", PageInfoUtil.PAGE_NUM);

        request.setAttribute("currentCategoryId",id);
        request.setAttribute("pn",pn);

        return "/post/category";
    }

    @RequestMapping("/post/{id:\\d}")
    public String detail(@PathVariable(name="id") Long id){
        System.out.println("method: detail " );

        PostVo postVo = postService.selectOnePost(new QueryWrapper<Post>().eq("p.id", id));
        Assert.notNull(postVo,"文章已被删除");
        postService.putViewCount(postVo);
        IPage<CommentVo> results = commentService.paging(getPage(), postVo.getId(), null, "created");

        request.setAttribute("currentCategoryId",postVo.getCategoryId());
        request.setAttribute("post",postVo);
        request.setAttribute("pageData",results);
        return "/post/detail";
    }

    @RequestMapping("/post/edit")
    public String edit(){
        System.out.println("/post/edit" );

        String id = request.getParameter("id");
        System.out.println("id： " + id);

        if(StringUtils.isNotEmpty(id)){
            Post post = postService.getById(id);
            Assert.isTrue(post != null,"该帖子已经删除");
            Assert.isTrue(post.getUserId().equals(getProfileId()),"当前用户没有权限操作此文章");

            request.setAttribute("post",post);
        }

        request.setAttribute("categories",categoryService.list());

        return "/post/edit";
    }


    @ResponseBody
    @PostMapping("/post/submit")
    public Result submit(Post post) {
        ValidationUtil.ValidResult validResult = ValidationUtil.validateBean(post);
        if(validResult.hasErrors()) {
            return Result.fail(validResult.getErrors());
        }

        if(post.getId() == null) {
            post.setUserId(getProfileId());

            post.setModified(new Date());
            post.setCreated(new Date());
            post.setCommentCount(0);
            post.setEditMode(null);
            post.setLevel(0);
            post.setRecommend(false);
            post.setViewCount(0);
            post.setVoteDown(0);
            post.setVoteUp(0);
            postService.save(post);

        } else {
            Post tempPost = postService.getById(post.getId());
            Assert.isTrue(tempPost.getUserId().longValue() == getProfileId().longValue(), "无权限编辑此文章！");

            tempPost.setTitle(post.getTitle());
            tempPost.setContent(post.getContent());
            tempPost.setCategoryId(post.getCategoryId());
            postService.updateById(tempPost);
        }

        // 通知消息给mq，告知更新或添加
        amqpTemplate.convertAndSend(RabbitConfig.es_exchange, RabbitConfig.es_bind_key,
                new PostMqIndexMessage(post.getId(), PostMqIndexMessage.CREATE_OR_UPDATE));

        return Result.success().action("/post/" + post.getId());
    }


    @ResponseBody
    @PostMapping("/post/delete")
    public Result delete(Long id) {

        Post post = postService.getById(id);

        Assert.notNull(post, "该帖子已被删除");
        Assert.isTrue(post.getUserId().longValue() == getProfileId().longValue(), "无权限删除此文章！");

        postService.removeById(id);

        // 删除相关消息、收藏等
        userMessageService.removeByMap(MapUtil.of("post_id", id));
        userCollectionService.removeByMap(MapUtil.of("post_id", id));

        amqpTemplate.convertAndSend(RabbitConfig.es_exchange, RabbitConfig.es_bind_key,
                new PostMqIndexMessage(post.getId(), PostMqIndexMessage.REMOVE));

        return Result.success().action("/user/index");
    }


    @ResponseBody
    @PostMapping("/collection/find/")
    public Result collectionFind(Long pid){

        int count = userCollectionService.count(new QueryWrapper<UserCollection>()
                .eq("user_id", getProfileId())
                .eq("post_id", pid));

        return Result.success(MapUtil.of("collection",count > 0));
    }

    @ResponseBody
    @PostMapping("/collection/add/")
    public Result collectionAdd(Long pid){

        Post post = postService.getById(pid);

        Assert.isTrue(post != null, "改帖子已被删除");

        int count = userCollectionService.count(new QueryWrapper<UserCollection>()
                .eq("user_id", getProfileId())
                .eq("post_id", pid));

        if(count > 0){
            Result.fail("你已经收藏");
        }

        UserCollection userCollection = new UserCollection();

        userCollection.setPostId(pid);
        userCollection.setUserId(getProfileId());
        userCollection.setCreated(new Date());
        userCollection.setModified(new Date());
        userCollection.setPostUserId(post.getUserId());

        boolean save = userCollectionService.save(userCollection);

        return save ? Result.success() : Result.fail();
    }

    @ResponseBody
    @PostMapping("/collection/remove/")
    public Result collectionRemove(Long pid){

        Post post = postService.getById(pid);

        Assert.isTrue(post != null, "改帖子已被删除");


        boolean remove = userCollectionService.remove(new QueryWrapper<UserCollection>()
                .eq("user_id", getProfileId())
                .eq("post_id", pid));

        return remove ? Result.success() : Result.fail();
    }


    @ResponseBody
    @Transactional
    @PostMapping("/post/reply/")
    public Result reply(Long pid,String content){

        Assert.notNull(pid,"找不到对应文章");
        Assert.notNull(content,"评论内容不能为空");

        Post post = postService.getById(pid);

        Assert.isTrue(post != null, "改帖子已被删除");

        Comment comment = new Comment();
        comment.setPostId(pid);
        comment.setContent(content);
        comment.setUserId(getProfileId());
        comment.setCreated(new Date());
        comment.setModified(new Date());
        comment.setLevel(0);
        comment.setVoteDown(0);
        comment.setVoteUp(0);
        commentService.save(comment);

        post.setCommentCount(post.getCommentCount()+1);

        postService.updateById(post);

//        本周热议加1
        postService.incrComentCountAndUnionForWeekRank(post,true);

        //通知作者，有评论消息
        if(comment.getUserId() != post.getUserId()){
            UserMessage userMessage = new UserMessage();
            userMessage.setPostId(pid);
            userMessage.setCommentId(comment.getId());
            userMessage.setFromUserId(getProfileId());
            userMessage.setToUserId(post.getUserId());
            userMessage.setType(1);
            userMessage.setContent(content);
            userMessage.setCreated(new Date());
            userMessage.setStatus(0);
            userMessageService.save(userMessage);

            wsService.sendMessageCountToUser(userMessage.getToUserId());

        }

        if (content.startsWith("@")){
            String username = content.substring(1,content.indexOf(" "));
            System.out.println(username);

            User user = userService.getOne(new QueryWrapper<User>().eq("username", username));

            if(user!=null){
                UserMessage userMessage = new UserMessage();
                userMessage.setPostId(pid);
                userMessage.setCommentId(comment.getId());
                userMessage.setFromUserId(getProfileId());
                userMessage.setToUserId(user.getId());
                userMessage.setType(2);
                userMessage.setContent(content);
                userMessage.setCreated(new Date());
                userMessage.setStatus(0);
                userMessageService.save(userMessage);

                wsService.sendMessageCountToUser(user.getId());
            }
        }
      return  Result.success().action("/post/" + post.getId());
    }

    @ResponseBody
    @Transactional
    @PostMapping("/post/reply-delete/")
    public Result reply(Long id) {

        Assert.notNull(id, "评论id不能为空！");

        Comment comment = commentService.getById(id);

        Assert.notNull(comment, "找不到对应评论！");

        if(! comment.getUserId().equals(getProfileId())) {
            return Result.fail("不是你发表的评论！");
        }
        commentService.removeById(id);

        // 评论数量减一
        Post post = postService.getById(comment.getPostId());
        post.setCommentCount(post.getCommentCount() - 1);
        postService.saveOrUpdate(post);

        //本周热议评论数量减一
        postService.incrComentCountAndUnionForWeekRank(post, false);

        return Result.success(null);
    }



}
