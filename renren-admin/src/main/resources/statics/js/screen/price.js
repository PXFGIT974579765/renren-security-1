
var price_chart = echarts.init(document.getElementById("kline"));
var upColor = '#00da3c';
var upBorderColor = '#008F28';
var downColor = '#ec0000';
var downBorderColor = '#8A0000';

var name = $("#vename").val();
var area = $("#vearea").val();
var data0;
var data1;
var myDate = new Date();
var month=myDate.getMonth()+1;
var endDate = myDate.getFullYear() + '-' + month + "-" + myDate.getDate();

myDate.setMonth(month - 12);
var startDate = myDate.getFullYear() + '-' + myDate.getMonth()+ "-" + myDate.getDate();
$("select[name=vearea]").empty();

$("select[name=vearea]").change( function() {
    area = $("#vearea").val();
    updateItems();
});

$("select[name=vename]").change( function() {
    name = $("#vename").val();
    query();
});

$.ajax({
    url:"sys/vegetable/arealist",
    type:"post",
    dataType:"json",
    contentType: "application/json",
    traditional: true,
    success: function (data) {
        var optionstring ="";
        for (var i = 0; i < data.length;i++) {
            optionstring += "<option value=\"" + data[i] + "\" >" + data[i] + "</option>";

        }

        $("select[name=vearea]").append(optionstring);

        area = $("#webarea").val();
        $("select[name=vearea]").val(area);
        updateItems();
    },
    error: function (msg) {
        alert("出错了！");
    }
});


$("#datepicker").daterangepicker(
    {
        "showDropdowns" : true,
        "locale" : {

            "format" : "YYYY-MM-DD",
            "separator" : " - ",
            "applyLabel" : "确定",
            "cancelLabel" : "取消",
            "fromLabel" : "从",
            "toLabel" : "到",
            "customRangeLabel" : "自定义",
            "daysOfWeek" : [ "星期日", "星期一", "星期二", "星期三", "星期四",
                "星期五", "星期六" ],
            "monthNames" : [ "一月", "二月", "三月", "四月", "五月", "六月",
                "七月", "八月", "九月", "十月", "十一月", "十二月" ],
            "firstDay" : 1
        },
        "startDate": startDate,
        "endDate": endDate

    },

    function(start, end, label) {
        startDate = start.format('YYYY-MM-DD');
        endDate = end.format('YYYY-MM-DD');
        console.log('New date range selected: '
            + start.format('YYYY-MM-DD') + ' to '
            + end.format('YYYY-MM-DD') + ' (predefined range: '
            + label + ')');
        query();
    });
function query() {
    console.log(name+"   "+area);
    initdata();
}

function initdata(){
    $.get("sys/vegetable/listbycondition?name="+name+"&area="+area+"&beginTime="+startDate+"&endTime="+endDate,

        function(r) {
            data0 = splitData2(r);
            $.get("sys/vegetable/pre", function(r) {
                data1 = r;

                draw();
            })
        })
};
function splitData2(rawData) {
    var categoryData = [];
    var values = [];
    var hprice = [];
    var lprice = [];
    var hpreprice = [];
    var lpreprice = [];

    categoryData.push(rawData[0].time);
    values.push([ rawData[0].hPrice, rawData[0].hPrice,
        rawData[0].hPrice, rawData[0].hPrice ]);
    hprice.push([ rawData[0].hPrice, rawData[0].hPrice,
        rawData[0].hPrice, rawData[0].hPrice ]);
    lprice.push([ rawData[0].lPrice, rawData[0].lPrice,
        rawData[0].lPrice, rawData[0].lPrice ]);

    if (rawData[0].predicHPrice == null || rawData[0].predicHPrice == '' || rawData[0].predicHPrice == 0.0) {
        hpreprice.push('-');
    } else {
        hpreprice.push(rawData[0].predicHPrice);
    }
    if (rawData[0].predicLPrice == null || rawData[0].predicLPrice == '' || rawData[0].predicLPrice == 0.0) {
        lpreprice.push('-');
    } else {
        lpreprice.push(rawData[0].predicLPrice);
    }

    for (var i = 1; i < rawData.length; i++) {
        categoryData.push(rawData[i].time);
        values.push([ rawData[i].hPrice, rawData[i - 1].hPrice,
            rawData[i].hPrice, rawData[i - 1].hPrice ])
        hprice.push([ rawData[i].hPrice, rawData[i - 1].hPrice,
            rawData[i].hPrice, rawData[i - 1].hPrice ]);
        lprice.push([ rawData[i].lPrice, rawData[i - 1].lPrice,
            rawData[i].lPrice, rawData[i - 1].lPrice ]);
        if (rawData[i].predicHPrice == null
            || rawData[i].predicHPrice == 0.0
            || rawData[i].predicHPrice == '')  {
            hpreprice.push('-');
        } else {
            hpreprice.push((rawData[i].predicHPrice).toFixed(2));
        }
        if ( rawData[i].predicLPrice == null
            ||rawData[i].predicLPrice == ''
            || rawData[i].predicLPrice == 0.0) {
            lpreprice.push('-');
        } else {
            lpreprice.push((rawData[i].predicLPrice).toFixed(2));
        }
    }
    return {
        categoryData : categoryData,
        values : values,
        hprice : hprice,
        lprice : lprice,
        hpreprice : hpreprice,
        lpreprice : lpreprice
    };
}


function splitHistoryData(rdata) {
    var result = [];
    for (var i = 0, len = rdata.length; i < len; i++) {
        result.push(rdata[i][0]);
    }
    return result;
}


function draw() {
    var price_option = {
        /* backgroundColor: '#142058', */

        // title : {
        //     text : name+'价格态势波动',
        //     left : 0,
        //     textStyle: {
        //         fontWeight: 'normal',
        //         fontSize: 36,
        //         color: '#000000',
        //     }
        // },
        tooltip : {
            trigger : 'axis',
            axisPointer : {
                type : 'cross'
            }
        },
        legend : {
            data : [ '日最高价格波动', '日最低价格波动', '最高价格', '预测最高价格', '最低价格',
                '预测最低价格' ],
            textStyle: {
                fontSize: 12,
                color: '#000000',
            }
        },
        grid : {
            left : '10%',
            right : '10%',
            bottom : '15%'
        },
        xAxis : {
            type : 'category',
            data : data0.categoryData,
            scale : true,
            boundaryGap : false,
            axisLine : {
                onZero : false
            },
            axisLabel: {
                textStyle: {
                    color: '#000000',
                    fontSize: 16,
                }
            },
            splitLine : {
                show : false
            },
            splitNumber : 20,
            min : 'dataMin',
            max : 'dataMax',

        },
        yAxis : {
            scale : true,
            splitArea : {
                show : true
            },
            axisLabel: {
                textStyle: {
                    color: '#000000',
                    fontSize: 16,
                }
            },

        },
        dataZoom : [ {
            type : 'inside',
            start : 50,
            end : 100
        }, {
            show : true,
            type : 'slider',
            y : '90%',
            start : 50,
            end : 100
        } ],
        series : [
            {
                name : '日最高价格波动',
                type : 'candlestick',
                data : data0.hprice,
                itemStyle : {
                    normal : {
                        color : upColor,
                        color0 : downColor,
                        borderColor : upBorderColor,
                        borderColor0 : downBorderColor
                    }
                },
                markPoint : {
                    label : {
                        normal : {
                            formatter : function(param) {
                                return param != null ? Math
                                    .round(param.value) : '';
                            }
                        }
                    },
                    data : [ {
                        name : 'XX标点',
                        coord : [ '2013/5/31', 2300 ],
                        value : 2300,
                        itemStyle : {
                            normal : {
                                color : 'rgb(41,60,85)'
                            }
                        }
                    }, {
                        name : 'highest value',
                        type : 'max',
                        valueDim : 'highest'
                    }, {
                        name : 'lowest value',
                        type : 'min',
                        valueDim : 'lowest'
                    }, {
                        name : 'average value on close',
                        type : 'average',
                        valueDim : 'close'
                    } ],
                    tooltip : {
                        formatter : function(param) {
                            return param.name + '<br>'
                                + (param.data.coord || '');
                        }
                    }
                },
                markLine : {
                    symbol : [ 'none', 'none' ],
                    data : [ [ {
                        name : 'from lowest to highest',
                        type : 'min',
                        valueDim : 'lowest',
                        symbol : 'circle',
                        symbolSize : 10,
                        label : {
                            normal : {
                                show : false
                            },
                            emphasis : {
                                show : false
                            }
                        }
                    }, {
                        type : 'max',
                        valueDim : 'highest',
                        symbol : 'circle',
                        symbolSize : 10,
                        label : {
                            normal : {
                                show : false
                            },
                            emphasis : {
                                show : false
                            }
                        }
                    } ], {
                        name : 'min line on close',
                        type : 'min',
                        valueDim : 'close'
                    }, {
                        name : 'max line on close',
                        type : 'max',
                        valueDim : 'close'
                    } ]
                }
            },
            {
                name : '日最低价格波动',
                type : 'candlestick',
                data : data0.lprice,
                itemStyle : {
                    normal : {
                        color : '#de8d8c',
                        color0 : '#00f0f1',
                        borderColor : '#de8d8c',
                        borderColor0 : '#00f0f1'
                    }
                },
                markPoint : {
                    label : {
                        normal : {
                            formatter : function(param) {
                                return param != null ? Math
                                    .round(param.value) : '';
                            }
                        }
                    },
                    data : [ {
                        name : 'XX标点',
                        coord : [ '2013/5/31', 2300 ],
                        value : 2300,
                        itemStyle : {
                            normal : {
                                color : 'rgb(41,60,85)'
                            }
                        }
                    }, {
                        name : 'highest value',
                        type : 'max',
                        valueDim : 'highest'
                    }, {
                        name : 'lowest value',
                        type : 'min',
                        valueDim : 'lowest'
                    }, {
                        name : 'average value on close',
                        type : 'average',
                        valueDim : 'close'
                    } ],
                    tooltip : {
                        formatter : function(param) {
                            return param.name + '<br>'
                                + (param.data.coord || '');
                        }
                    }
                },
                markLine : {
                    symbol : [ 'none', 'none' ],
                    data : [ [ {
                        name : 'from lowest to highest',
                        type : 'min',
                        valueDim : 'lowest',
                        symbol : 'circle',
                        symbolSize : 10,
                        label : {
                            normal : {
                                show : false
                            },
                            emphasis : {
                                show : false
                            }
                        }
                    }, {
                        type : 'max',
                        valueDim : 'highest',
                        symbol : 'circle',
                        symbolSize : 10,
                        label : {
                            normal : {
                                show : false
                            },
                            emphasis : {
                                show : false
                            }
                        }
                    } ], {
                        name : 'min line on close',
                        type : 'min',
                        valueDim : 'close'
                    }, {
                        name : 'max line on close',
                        type : 'max',
                        valueDim : 'close'
                    } ]
                }
            }, {
                name : '最高价格',
                type : 'line',
                data : splitHistoryData(data0.hprice),
                smooth : true,
                lineStyle : {
                    normal : {
                        opacity : 1
                    }
                }
            }, {
                name : '预测最高价格',
                type : 'line',
                data : data0.hpreprice,
                smooth : true,
                color: '#f6e743',
                lineStyle : {
                    normal : {
                        opacity : 1
                    },

                }
            }, {
                name : '最低价格',
                type : 'line',
                data : splitHistoryData(data0.lprice),
                smooth : true,
                color: '#9a94da',
                lineStyle : {
                    normal : {
                        opacity : 1
                    }
                }
            }, {
                name : '预测最低价格',
                type : 'line',
                data : data0.lpreprice,
                smooth : true,
                color: '#f848fa',
                lineStyle : {
                    normal : {
                        opacity : 1
                    }
                }
            },

        ]
    };

    ;
    if (price_option && typeof price_option === "object") {
        price_chart.setOption(price_option, true);
    }
}
//初始化蔬菜种类列表
function updateItems(){
    $("select[name=vename]").empty();
    $.ajax({
        url:"sys/vegetable/itemlist?area="+area,
        type:"post",
        dataType:"json",
        contentType: "application/json",
        traditional: true,
        success: function (data) {
            var optionstring ="";

            for (var i = 0; i < data.length;i++) {
                optionstring += "<option value=\"" + data[i] + "\" >" + data[i] + "</option>";

            }

            $("select[name=vename]").append(optionstring);

            name = $("#webname").val();
            $("select[name=vename]").val(name);
            $('#ve-title').text(name+"价格态势波动");
            initdata();
        },
        error: function (msg) {
            alert("出错了！");
        }
    });


};