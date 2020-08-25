<#include "/inc/layout.ftl" />

<@layout title="博客分类">

  <#include "/inc/header-panel.ftl" />

<div class="layui-container">
  <div class="layui-row layui-col-space15">

    <div class="layui-col-md8">
      <div class="fly-panel" style="margin-bottom: 0;">
        
        <div class="fly-panel-title fly-filter">
          <a href="" class="layui-this">综合</a>
          <span class="fly-mid"></span>
          <a href="">未结</a>
          <span class="fly-mid"></span>
          <a href="">已结</a>
          <span class="fly-mid"></span>
          <a href="">精华</a>
          <span class="fly-filter-right layui-hide-xs">
            <a href="" class="layui-this">按最新</a>
            <span class="fly-mid"></span>
            <a href="">按热议</a>
          </span>
        </div>



        <@posts pn=pn size=2 level=1 categoryId=currentCategoryId >

          <ul class="fly-list">

            <#list results.records as post>

              <@plisting post></@plisting>

            </#list>
          </ul>
          <@paging results> </@paging>
        </@posts>

        
        <!-- <div class="fly-none">没有相关数据</div> -->



      </div>
    </div>

    <#include "/inc/right.ftl" />

  </div>
</div>

</@layout>