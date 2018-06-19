package io.renren.modules.job.service.impl;

import io.renren.modules.job.service.RestTemplateService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by pxf on 2018-6-14
 */
@Service("restTemplateService")
public class RestTemplateServiceImpl implements RestTemplateService {

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public <T> T getForObject(String url, Map map) {
        return null;
    }

    @Override
    public <T> T postForObject(String url, HashMap<String,String> map, Class<T> clas) {
        if(StringUtils.isEmpty(url)){
            return null;
        }
        if(!url.startsWith("http")){
            url="http://"+url;
        }
        T result = restTemplate.postForEntity(url, map, clas).getBody();
        return result;
    }
}