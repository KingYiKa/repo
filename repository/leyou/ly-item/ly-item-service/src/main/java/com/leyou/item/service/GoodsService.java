package com.leyou.item.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.vo.PageResult;
import com.leyou.item.mapper.SkuMapper;
import com.leyou.item.mapper.SpuDetailMapper;
import com.leyou.item.mapper.SpuMapper;
import com.leyou.item.mapper.StockMapper;
import com.leyou.item.pojo.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.*;
import java.util.stream.Collectors;

/**商品仓库
 * @author wangshiyu
 */
@Service
public class GoodsService {
    @Autowired
    private SpuMapper spuMapper;
    @Autowired
    private SpuDetailMapper detailMapper;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private BrandService brandService;
    @Autowired
    private SkuMapper skuMapper;
    @Autowired
    private StockMapper stockMapper;


    public PageResult<Spu> querySpuByPage(Integer page, Integer rows, Boolean saleable, String key) {
        //分页
        PageHelper.startPage(page,rows);
        //过滤
        Example example = new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();
        //搜索字段过滤
        if (StringUtils.isNoneBlank(key)){
            criteria.andLike("title","%" + key + "%");
        }
        //上下架过滤
        if (saleable != null){
            criteria.andEqualTo("saleable",saleable);
        }
        //默认排序
        example.setOrderByClause("last_update_time DESC");

        //查询
        Page<Spu> spus = (Page<Spu>) spuMapper.selectByExample(example);
        //List<Spu> spus = spuMapper.selectByExample(example);
        //判断
        if (CollectionUtils.isEmpty(spus)){
            throw new LyException(ExceptionEnum.GOODS_NOT_FOND);
        }
        //解析分类和品牌的名称
        loadCategoryAndBrandName(spus);

        //解析分页结果
        PageInfo<Spu> info = new PageInfo<Spu>(spus);

        return new PageResult<>(info.getTotal(),spus);

    }

    private void loadCategoryAndBrandName(Page<Spu> spus) {
        for (Spu spu:spus) {
            //处理分类名称
            List<String> names = categoryService.queryById(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()))
                    .stream().map(Category::getName).collect(Collectors.toList());
            spu.setCname(StringUtils.join(names,"/"));
            //处理品牌名称
            spu.setBname(brandService.queryById(spu.getBrandId()).getName());

        }

    }

    @Transactional(rollbackFor=Exception.class)
    public void saveGoods(Spu spu) {
        //新增spu
        spu.setId(null);
        spu.setCreateTime(new Date());
        spu.setLastUpdateTime(spu.getCreateTime());
        spu.setSaleable(true);
        spu.setValid(false);

        int count = spuMapper.insert(spu);
        if (count != 1){
            throw new LyException(ExceptionEnum.GOODS_SAVE_ERROR);
        }
        //新增detail
        SpuDetail detail = spu.getSpuDetail();
        detail.setSpuId(spu.getId());
        detailMapper.insert(detail);

        //新增sku和库存
        saveSkuAndStock(spu);

    }

    private void saveSkuAndStock(Spu spu) {
        //定义库存结合
        int count;
        List<Stock> stockList = new ArrayList<>();

        //新增sku
        List<Sku> skus = spu.getSkus();

        for (Sku sku:skus) {
            sku.setSpuId(spu.getId());
            sku.setCreateTime(new Date());
            sku.setLastUpdateTime(sku.getCreateTime());

            count = skuMapper.insert(sku);

            if (count != 1){
                throw new LyException(ExceptionEnum.GOODS_SAVE_ERROR);
            }

            //新增库存
            Stock stock = new Stock();
            stock.setSkuId(sku.getId());
            stock.setStock(sku.getStock());

            stockList.add(stock);
        }

        //批量新增
        count = stockMapper.insertList(stockList);
        if (count != stockList.size()){
            throw new LyException(ExceptionEnum.GOODS_SAVE_ERROR);
        }
    }


    public SpuDetail queryDetailById(Long spuId) {
        SpuDetail detail = detailMapper.selectByPrimaryKey(spuId);
        if (detail == null){
            throw new LyException(ExceptionEnum.GOODS_DETAIL_NOT_FOND);
        }
        return detail;
    }

    public List<Sku> querySkuBySkuId(Long spuId) {
        //查询sku
        Sku sku = new Sku();
        sku.setSpuId(spuId);
        List<Sku> skuList = skuMapper.select(sku);
        if (CollectionUtils.isEmpty(skuList)){
            throw new LyException(ExceptionEnum.GOODS_SKU_NOT_FOND);
        }
        //查询库存
//        for (Sku s : skuList) {
//            Stock stock = stockMapper.selectByPrimaryKey(s.getId());
//            if (stock == null){
//                throw new LyException(ExceptionEnum.GOODS_STOCK_NOT_FOND);
//            }
//            s.setStock(stock.getStock());
//        }

        //查询库存
        List<Long> ids = skuList.stream().map(Sku::getId).collect(Collectors.toList());
        List<Stock> stockList = stockMapper.selectByIdList(ids);
        if (CollectionUtils.isEmpty(stockList)){
                throw new LyException(ExceptionEnum.GOODS_STOCK_NOT_FOND);
        }

        //我们把stock变成一个map，其key是：sku的id，值是库存值
        Map<Long, Integer> stockMap = stockList.stream().collect(Collectors.toMap(Stock::getSkuId, Stock::getStock));

        skuList.forEach(s -> s.setStock(stockMap.get(s.getId())));

        return skuList;
    }

    @Transactional(rollbackFor=Exception.class)
    public void updateGoods(Spu spu) {
        if (spu.getId()==null){
            throw new LyException(ExceptionEnum.GOODS_ID_CANNOT_BY_NULL);
        }
        Sku sku = new Sku();
        sku.setSpuId(spu.getId());
        //查询sku
        List<Sku> skuList = skuMapper.select(sku);
        if (!CollectionUtils.isEmpty(skuList)){
            //删除sku
            skuMapper.delete(sku);
            //删除stock
            List<Long> ids = skuList.stream().map(Sku::getId).collect(Collectors.toList());
            stockMapper.deleteByIdList(ids);
        }
        //修改spu
        spu.setValid(null);
        spu.setSaleable(null);
        spu.setLastUpdateTime(new Date());
        spu.setCreateTime(null);

        int count = spuMapper.updateByPrimaryKeySelective(spu);
        if(count != 1){
            throw new LyException(ExceptionEnum.GOODS_UPDATE_ERROR);
        }
        //修改detail
        count = detailMapper.updateByPrimaryKeySelective(spu.getSpuDetail());
        if(count != 1){
            throw new LyException(ExceptionEnum.GOODS_UPDATE_ERROR);
        }
        //新增sku和stock
        saveSkuAndStock(spu);
    }
}
