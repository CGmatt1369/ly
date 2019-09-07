package com.leyou.search.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.BeanHelper;
import com.leyou.common.utils.JsonUtils;
import com.leyou.common.vo.PageResult;
import com.leyou.item.client.ItemClient;
import com.leyou.item.pojo.DTO.*;
import com.leyou.search.dto.GoodsDTO;
import com.leyou.search.dto.SearchRequest;
import com.leyou.search.pojo.Goods;
import com.leyou.search.repository.GoodRepository;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.LongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilterBuilder;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;


import java.util.*;
import java.util.stream.Collectors;

/**
 * @创建人 zhenzekun
 * @创建时间 2019/9/3
 * @描述
 */

@Service
public class SearchService {
    //使用item提供的feign接口
    @Autowired
    private ItemClient itemClient;
    @Autowired
    private ElasticsearchTemplate esTemplate;

    //建一个基类的query
    public QueryBuilder buildBasicQuery(SearchRequest request){
        //搜索的关键词
        String key = request.getKey();
        Map<String, String> filterMap = request.getFilter();
        //        构造 布尔查询
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        queryBuilder.must(QueryBuilders.matchQuery("all",key).operator(Operator.AND));
        if(!CollectionUtils.isEmpty(filterMap)){
            for (String filterKey : filterMap.keySet()) {
                String fieldName = "specs."+filterKey;
                if(filterKey.equals("分类")){
                    fieldName = "categoryId";
                }
                else if(filterKey.equals("品牌")){
                    fieldName = "brandId";
                }
                //选择的过滤条件
                String value = filterMap.get(filterKey);
                queryBuilder.filter(QueryBuilders.termQuery(fieldName,value));
            }
        }
        return queryBuilder;
    }

    //查询可以存到es的数据，并将数据转换的Goods
    public Goods buildGoods(SpuDTO spu) {
        /*用于搜索相关的信息*/
        //spuid
        Long spuId = spu.getId();
        //subtitle
        String subTitle = spu.getSubTitle();
        //spuskus
        //1 通过spuid得到sku的集合
        List<SkuDTO> skuDTOList = itemClient.findSkuBySpuId(spuId);
        //2 拿到sku中需要的数据 skuid，title,image,price
        List<Map<String, Object>> skuMap = new ArrayList<>();
        for (SkuDTO skuDTO : skuDTOList) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", skuDTO.getId());
            map.put("price", skuDTO.getPrice());
            map.put("title", skuDTO.getTitle());
            //image只拿第一张 lang3包下
            map.put("image", StringUtils.substringBefore(skuDTO.getImages(), ","));
            skuMap.add(map);
        }
        //3 转成string json
        String skus = JsonUtils.toString(skuMap);
        //all 是用来拼接可搜索的关键词 分类名 品牌名 商品名称
        //1 分类名
        String categoryName = itemClient.findCategoryByIds(spu.getCategoryIds()).stream().map(CategoryDTO::getName)
                .collect(Collectors.joining(","));
        //2 品牌名
        String brandName = itemClient.findBrandById(spu.getBrandId()).getName();
        //3商品名称
        String spuName = spu.getName();
        //4 拼接
        String all = spuName + "," + categoryName + "," + brandName;
        //brandid
        Long brandId = spu.getBrandId();
        //categoryId
        Long categoryId = spu.getCid3();
        //createTime
        long createTime = spu.getCreateTime().getTime();
        //price 是set结构 sku集合可以得到价格集合
        Set<Long> price = skuDTOList.stream().map(SkuDTO::getPrice).collect(Collectors.toSet());
        //specs 规格参数map
        //1 定义一个map
        Map<String, Object> specs = new HashMap<>();
        //2 获得spec_param  并且是用于过滤的规格参数
        List<SpecParamDTO> specParams = itemClient.findSpecParams(null, categoryId, null);
        //3 获得spu_detail
        SpuDetailDTO spuDetailDTO = itemClient.findSpuDetailById(spuId);
        //3.1得到通用规格参数  json转换 用map<ID 和 值>
        String genericSpec = spuDetailDTO.getGenericSpec();
        Map<Long, Object> generic = JsonUtils.toMap(genericSpec, Long.class, Object.class);
        //3.2得到特有的规格参数
        String specialSpec = spuDetailDTO.getSpecialSpec();
        //map里面有list
        Map<Long, List<String>> special = JsonUtils.nativeRead(specialSpec, new TypeReference<Map<Long, List<String>>>() {
        });
        //4 存入map
        for (SpecParamDTO specParam : specParams) {
            //得到规格参数名字作为key
            String key = specParam.getName();
            //是指value 规格参数的值
            Object value = null;
            if (specParam.getGeneric()) {//是通用规格
                value = generic.get(specParam.getId());
            } else {
                value = special.get(specParam.getId());
            }
            //判断是否是数字类型，如果是的话，就需要处理 值的区间
            Boolean isNumeric = specParam.getIsNumeric();
            if (isNumeric) {
                value = chooseSegment(value, specParam);
            }
            specs.put(key, value);
        }

        Goods goods = new Goods();
        goods.setId(spuId);
        goods.setSubTitle(subTitle);
        goods.setSkus(skus);
        goods.setAll(all);
        goods.setBrandId(brandId);
        goods.setCategoryId(categoryId);
        goods.setCreateTime(createTime);
        goods.setPrice(price);
        goods.setSpecs(specs);
        return goods;
    }
    /*数字分段*/
    private String chooseSegment(Object value, SpecParamDTO p) {
        if (value == null || StringUtils.isBlank(value.toString())) {
            return "其它";
        }
        double val = parseDouble(value.toString());
        String result = "其它";
        // 保存数值段
        //value = 3000
        //0-2000,2000-3000,3000-4000,4000-
        for (String segment : p.getSegments().split(",")) {
            String[] segs = segment.split("-");
            // 获取数值范围
            double begin = parseDouble(segs[0]);
            double end = Double.MAX_VALUE;
            if (segs.length == 2) {
                end = parseDouble(segs[1]);
            }
            // 判断是否在范围内
            if (val >= begin && val < end) {
                if (segs.length == 1) {
                    result = segs[0] + p.getUnit() + "以上";
                } else if (begin == 0) {
                    result = segs[1] + p.getUnit() + "以下";
                } else {
                    result = segment + p.getUnit();
                }
                break;
            }
        }
        return result;
    }
    private double parseDouble(String str) {
        try {
            return Double.parseDouble(str);
        } catch (Exception e) {
            return 0;
        }
    }
    public PageResult<GoodsDTO> search(SearchRequest request) {
        //搜索条件判断
        String key = request.getKey();
        if (StringUtils.isBlank(key)){
            throw new LyException(ExceptionEnum.INVALID_PARAM_ERROR);
        }

        //构建原生构建器
        NativeSearchQueryBuilder queryBuilder=new NativeSearchQueryBuilder();
            //过滤
        queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{"id","subTitle","skus"},null));
        //查询
//        queryBuilder.withQuery(QueryBuilders.matchQuery("all", key).operator(Operator.AND));
        queryBuilder.withQuery(this.buildBasicQuery(request));
        //分页
        int page = request.getPage() - 1;
        int size = request.getSize();
        queryBuilder.withPageable(PageRequest.of(page,size));
        //搜索
        AggregatedPage<Goods> result = esTemplate.queryForPage(queryBuilder.build(), Goods.class);
        //解析结果
        long total = result.getTotalElements();
        int totalPages = result.getTotalPages();
        List<Goods> list = result.getContent();
        //转成dto
        List<GoodsDTO> goodsDTOS = BeanHelper.copyWithCollection(list, GoodsDTO.class);
        return new PageResult<>(goodsDTOS,total,totalPages);
    }
/*过滤查询*/
    public Map<String, List<?>> queryFilters(SearchRequest request) {
        Map<String,List<?>> filterMap=new LinkedHashMap<>();
        String key = request.getKey();
        //构建原声查询
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        //过滤结果
        queryBuilder.withSourceFilter(new FetchSourceFilterBuilder().build());
        //关键字chaxun
        queryBuilder.withQuery(this.buildBasicQuery(request));
        //处理分页 就一条数据（不需要数据）
        queryBuilder.withPageable(PageRequest.of(0,1));
        //配置聚合
        queryBuilder.addAggregation(AggregationBuilders.terms("categoryAgg").field("categoryId"));//分类聚合
        queryBuilder.addAggregation(AggregationBuilders.terms("brandAgg").field("brandId"));//品牌聚合
        //聚合query对象给es并返回
        AggregatedPage<Goods> aggregatedPage = esTemplate.queryForPage(queryBuilder.build(), Goods.class);
        Aggregations aggregations = aggregatedPage.getAggregations();//所有的聚合对象
        //获得categoryAgg的聚合结果
        LongTerms categoryTerms = aggregations.get("categoryAgg");
        //调用handleCategory方法 将结果放到filterMap中
//            根据是否选择分类id，来展示规格参数
        List<Long> idList = handleCategory(categoryTerms, filterMap);
        if (idList!=null && idList.size()==1){
            handleSpecAgg(idList.get(0),request,filterMap);
        }
        //获得品牌的结果
        LongTerms brandTerms = aggregations.get("brandAgg");
        handleBrand(brandTerms,filterMap);
        return filterMap;
    }
    private void handleSpecAgg(Long cid, SearchRequest request, Map<String, List<?>> filterMap) {
        // 1.查询分类下需要搜索过滤的规格参数名称
        List<SpecParamDTO> specParams = itemClient.findSpecParams(null, cid, true);
        //  原生查询
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        //查询条件
        queryBuilder.withQuery(this.buildBasicQuery(request));
        queryBuilder.withSourceFilter(new FetchSourceFilterBuilder().build());
        queryBuilder.withPageable(PageRequest.of(0,1));

        //聚合
        for (SpecParamDTO specParam : specParams) {
            //获得name
            String name = specParam.getName();
            queryBuilder.addAggregation(AggregationBuilders.terms(name).field("specs." + name));
        }
        //查询es 并返回
        AggregatedPage<Goods> result = esTemplate.queryForPage(queryBuilder.build(), Goods.class);
        Aggregations aggregations = result.getAggregations();//全部分同信息
        //再次遍历specParams 得到桶名
        for (SpecParamDTO specParam : specParams) {
            String name = specParam.getName();
            StringTerms terms = aggregations.get(name);//获得聚合信息
            //规格参数的聚合结果
            List<String> paramValues = terms.getBuckets().stream().map(StringTerms.Bucket::getKeyAsString)
                    .filter(StringUtils::isNoneEmpty)
                    .collect(Collectors.toList());
            //存到map
            filterMap.put(name,paramValues);
        }

    }
    private void handleBrand(LongTerms brandTerms, Map<String, List<?>> filterMap) {
        List<Long> idList = brandTerms.getBuckets().stream()
                .map(LongTerms.Bucket::getKeyAsNumber)
                .map(Number::longValue)
                .collect(Collectors.toList());
        //根据id查询品牌
        List<BrandDTO> brandsByIds = itemClient.findBrandsByIds(idList);
        //存入filterMap
        filterMap.put("品牌",brandsByIds);
    }
    private List<Long> handleCategory(LongTerms categoryTerms, Map<String, List<?>> filterMap) {
        List<Long> idList = categoryTerms.getBuckets().stream()
                .map(LongTerms.Bucket::getKeyAsNumber)
                .map(Number::longValue)
                .collect(Collectors.toList());
        List<CategoryDTO> categoryByIds = itemClient.findCategoryByIds(idList);
        filterMap.put("分类",categoryByIds);
        return idList;
    }
}