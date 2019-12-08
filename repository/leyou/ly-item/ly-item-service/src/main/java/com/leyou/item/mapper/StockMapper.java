package com.leyou.item.mapper;


import com.leyou.item.pojo.Stock;
import tk.mybatis.mapper.additional.idlist.DeleteByIdListMapper;
import tk.mybatis.mapper.additional.idlist.SelectByIdListMapper;
import tk.mybatis.mapper.additional.insert.InsertListMapper;
import tk.mybatis.mapper.common.Mapper;

/**
 * @author wangshiyu
 */
public interface StockMapper extends Mapper<Stock>,InsertListMapper<Stock>, SelectByIdListMapper<Stock,Long>, DeleteByIdListMapper<Stock,Long> {
}
