package com.wintersun.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wintersun.entity.UserMessage;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wintersun.vo.UserMessageVo;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author wintersun
 * @since 2020-07-22
 */
public interface UserMessageMapper extends BaseMapper<UserMessage> {

    IPage<UserMessageVo> selectMessages(Page page, @Param(Constants.WRAPPER) QueryWrapper<UserMessage> wrapper);

    @Transactional
    @Update("update user_message set status = 1 ${ew.customSqlSegment}")
    void updateToReaded(@Param(Constants.WRAPPER)QueryWrapper<UserMessage> wrapper);

}
