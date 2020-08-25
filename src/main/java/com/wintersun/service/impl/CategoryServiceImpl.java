package com.wintersun.service.impl;

import com.wintersun.entity.Category;
import com.wintersun.mapper.CategoryMapper;
import com.wintersun.service.CategoryService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author wintersun
 * @since 2020-07-22
 */
@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

}
