<!-- 地图渲染 -->
//地图容器
var map_chart = echarts.init(document.getElementById('map'));
//34个省、市、自治区的名字拼音映射数组
var provinces = {
    //23个省
    "台湾": "taiwan",
    "河北": "hebei",
    "山西": "shanxi",
    "辽宁": "liaoning",
    "吉林": "jilin",
    "黑龙江": "heilongjiang",
    "江苏": "jiangsu",
    "浙江": "zhejiang",
    "安徽": "anhui",
    "福建": "fujian",
    "江西": "jiangxi",
    "山东": "shandong",
    "河南": "henan",
    "湖北": "hubei",
    "湖南": "hunan",
    "广东": "guangdong",
    "海南": "hainan",
    "四川": "sichuan",
    "贵州": "guizhou3",
    "云南": "yunnan",
    "陕西": "shanxi1",
    "甘肃": "gansu",
    "青海": "qinghai",
    //5个自治区
    "新疆": "xinjiang",
    "广西": "guangxi",
    "内蒙古": "neimenggu",
    "宁夏": "ningxia",
    "西藏": "xizang",
    //4个直辖市
    "北京": "beijing",
    "天津": "tianjin",
    "上海": "shanghai",
    "重庆": "chongqing",
    //2个特别行政区
    "香港": "xianggang",
    "澳门": "aomen"
};
//地图标点
var geoCoordMap2 = {
    "成都聚合国际果蔬交易中心": [104.308427, 30.594861]
};
//因为数据比较多，这里只是举例参考
var corddata = [
    {
        "name": "四川成都龙泉聚和(国际)果蔬菜交易中心",
        "value": "500.00"
    }, {
        "name": "重庆双福国际农贸城",
        "value": "500.00"
    }];
var geoCoordMap = {
    '四川成都龙泉聚和(国际)果蔬菜交易中心': [104.308427, 30.594861],
    '重庆双福国际农贸城': [106.302263, 29.43251],
}

var convertData = function (data) {
    var res = [];
    for (var i = 0; i < data.length; i++) {
        var geoCoord = geoCoordMap[data[i].name];
        if (geoCoord) {
            res.push({
                name: data[i].name,
                value: geoCoord.concat(data[i].value)
            });
        }
    }
    return res;
};

//直辖市和特别行政区-只有二级地图，没有三级地图
var special = ["北京", "天津", "上海", "重庆", "香港", "澳门"];
var mapdata = [];
//绘制全国地图
$.getJSON('statics/map/china-smooth.json', function (data) {
    d = [];
    for (var i = 0; i < data.features.length; i++) {
        d.push({
            name: data.features[i].properties.name
        })
    }
    mapdata = d;
    //注册地图
    echarts.registerMap('中国', data, {

    });
    //绘制地图
    renderMap('中国', d);
});

//地图点击事件
map_chart.on('click', function (params) {
    console.log(params);
    if (params.name in provinces) {
        //如果点击的是34个省、市、自治区，绘制选中地区的二级地图
        $.getJSON('statics/map/province/' + provinces[params.name]
            + '.json', function (data) {
            echarts.registerMap(params.name, data);
            var d = [];
            for (var i = 0; i < data.features.length; i++) {
                d.push({
                    name: data.features[i].properties.name
                })
            }
            renderMap(params.name, d);
        });
    } else if ((params.name in cityMap)) {
        //如果是【直辖市/特别行政区】只有二级下钻
        if (special.indexOf(params.name) >= 0) {
            renderMap('中国', mapdata);
        } else {
            //显示县级地图
            $.getJSON('statics/map/city/' + cityMap[params.name]
                + '.json', function (data) {
                echarts.registerMap(params.name, data);
                var d = [];
                for (var i = 0; i < data.features.length; i++) {
                    d.push({
                        name: data.features[i].properties.name
                    })
                }

                renderMap(params.name, d);
            });
        }
    } else if (params.name in geoCoordMap) {
        parent.location.href = "screen?days=30&name=土豆&area="
            + params.name;
    } else {
        renderMap('中国', mapdata);
    }
});

//初始化绘制全国地图配置
var map_option = {
    backgroundColor: '#fff',
    title: {
        text: '蔬菜市场行情',

        left: 'center',
        textStyle: {
            color: '#000',
            fontSize: 16,
            fontWeight: 'normal',
            fontFamily: "Microsoft YaHei"
        },
        subtextStyle: {
            color: '#000',
            fontSize: 13,
            fontWeight: 'normal',
            fontFamily: "Microsoft YaHei"
        }
    },
    tooltip: {
        trigger: 'item',
        formatter: '{b}'
    },
    toolbox: {
        show: true,
        orient: 'vertical',
        left: 'right',
        top: 'center',
        feature: {
            dataView: {
                readOnly: false
            },
            restore: {},
            saveAsImage: {}
        },
        iconStyle: {
            normal: {
                color: '#000'
            }
        }
    },
    animationDuration: 1000,
    animationEasing: 'cubicOut',
    animationDurationUpdate: 1000

};
function renderMap(map, d) {
    map_option.title.subtext = map;
    map_option.geo = {
        map: map,
        // roam: true,
        //zoom: 1,
        label: {
            normal: {
                show: true,
                textStyle: {
                    color: '#000'
                }
            },
            emphasis: {
                textStyle: {
                    color: '#000'
                }
            }
        },
        itemStyle: {
            normal: {
                borderColor: 'rgba(147, 235, 248, 1)',
                borderWidth: 1,
                areaColor: {
                    type: 'radial',
                    x: 0.5,
                    y: 0.5,
                    r: 0.8,
                    colorStops: [{
                        offset: 0,
                        color: 'rgba(147, 235, 248, 0)' // 0% 处的颜色
                    }, {
                        offset: 1,
                        color: 'rgba(147, 235, 248, .2)' // 100% 处的颜色
                    }],
                    globalCoord: false
                    // 缺省为 false
                },
                shadowColor: 'rgba(128, 217, 248, 1)',
                // shadowColor: 'rgba(255, 255, 255, 1)',
                shadowOffsetX: -2,
                shadowOffsetY: 2,
                shadowBlur: 10
            },
            emphasis: {
                areaColor: '#aabc55',
                borderWidth: 0
            }
        },
        data: d,

    };
    map_option.series = [{
        name: 'mark',
        type: 'effectScatter',
        coordinateSystem: 'geo',
        data: convertData(corddata),
        symbolSize: 12,
        showEffectOn: 'render',
        rippleEffect: {
            brushType: 'stroke'
        },
        hoverAnimation: true,
        label: {
            normal: {
                formatter: '{b}',
                show: false
            },
            emphasis: {
                show: true
            }
        },
        itemStyle: {
            normal: {
                color: 'orange',
                shadowBlur: 10,
                shadowColor: 'orange'
            },
            emphasis: {
                borderColor: '#fff',
                borderWidth: 2
            }
        },

    }, {
        name: '点',
        type: 'scatter',
        coordinateSystem: 'geo',
        symbol: 'pin', //气泡
        symbolSize: 60,
        label: {
            normal: {
                show: false,
                textStyle: {
                    color: '#fff',
                    fontSize: 9,
                }
            }
        },
        itemStyle: {
            normal: {
                color: '#F62157', //标志颜色
            }
        },
        zlevel: 6,
        data: convertData(corddata),
    },
    ];
    //渲染地图
    map_chart.setOption(map_option);

}