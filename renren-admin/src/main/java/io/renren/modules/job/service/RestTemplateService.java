package io.renren.modules.job.service;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by hunter on 2018-6-14
 */
public interface RestTemplateService {

    <T> T getForObject(String url, Map map);
    <T> T postForObject(String url, HashMap<String,String> map, Class<T> clas);

}
