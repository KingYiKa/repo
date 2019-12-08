package com.leyou.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author wangshiyu
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public enum ExceptionEnum {

    //PRICE_CANNOT_BE_NULL(400,"价格不能为空")
    BRAND_NOT_FOUND(404,"品牌不存在"),
    CATEGORY_NOT_FOND(404,"商品分类没有查到"),
    SPEC_GROUP_NOT_FOND(404,"商品规格组不存在"),
    SPEC_PARAM_NOT_FOND(404,"商品规格参数不存在"),
    GOODS_NOT_FOND(404,"商品不存在"),
    GOODS_DETAIL_NOT_FOND(404,"商品详情不存在"),
    GOODS_SKU_NOT_FOND(404,"商品SKU不存在"),
    GOODS_STOCK_NOT_FOND(404,"商品库存不存在"),
    BRAND_SAVE_ERROR(500,"新增品牌失败"),
    CATEGORY_BRAND_SAVE_ERROR(500,"新增品牌分类失败"),
    UPLOAD_FILE_ERROR(500,"文件上传失败"),
    INVALID_FIE_TYPE(400,"无效的文件类型"),
    GOODS_SAVE_ERROR(500,"新增商品失败"),
    GOODS_UPDATE_ERROR(500,"修改商品失败"),
    GOODS_ID_CANNOT_BY_NULL(400,"商品id不能为空"),
    ;
    private int code;
    private String msg;

}
