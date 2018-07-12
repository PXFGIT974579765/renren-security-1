{
    var ps_days = $("#webdays").val();
    var ps_area = $("#webarea").val();
    var now = new Date(); // 这个算法能自动处理闰年和非闰年。2012年是闰年，所以2月有29号
    var stime = [];
    var i = 0;
    while (i < ps_days) {

        stime.unshift(now.getFullYear() + '-'
            + fillZero((now.getMonth() + 1)) + '-'
            + fillZero(now.getDate()));
        now = new Date(now - 24 * 60 * 60 * 1000); // 这个是关键！！！减去一天的毫秒数效果就是把日期往前推一天
        i++;

    }

    function fillZero(s) {
        //console.log(s);
        if (s < 10)
            s = '0' + s
        return s;
    }
}
<!-- 正负交叉图 -->
{
    var ps_chart = echarts.init(document.getElementById("price-situation"));
    var ps_area = $("#webarea").val();
    var threeAgoWhiteBar;
    var threeAgoHPriceBar;
    var predictWhiteBar;
    var predictHPriceBar;
    var lengendArr = ["前三天价格范围", "预测价格范围"];
    var categoryArr;
    var ps_data = {};
    var ps_data0;

    $("select[name=vearea]").change(function () {
        ps_area = $("#vearea").val();
        ps_updateItems();
    });

    $.ajax({
        url: "sys/vegetable/arealist",
        type: "post",
        dataType: "json",
        contentType: "application/json",
        traditional: true,
        success: function (data) {
            var optionstring = "";
            for (var i = 0; i < data.length; i++) {
                optionstring += "<option value=\"" + data[i] + "\" >"
                    + data[i] + "</option>";

            }

            $("select[name=vearea]").append(optionstring);

            ps_area = $("#webarea").val();
            $("select[name=vearea]").val(ps_area);
            ps_updateItems();
        },
        error: function (msg) {
            alert("出错了！");
        }
    });

    function ps_query() {
        console.log(name + "   " + ps_area);
        ps_initdata();
    }

    function ps_initdata() {
        $.get("sys/vegetable/listbyarea?area=" + ps_area,

            function (r) {
                //alert(JSON.stringify(r));
                ps_data0 = psSplitData(r);
                psDraw();
            })
    };
    Array.prototype.contains = function (element) {
        for (var i = 0; i < this.length; i++) {
            if (this[i] == element) {
                return true;
            }
        }
    }

    function psSplitData(rawData) {
        var categoryData = [];
        var threeAgoLPriceData = [];
        var threeAgoHPriceData = [];
        var predictLPriceData = [];
        var predictHPriceData = [];
        for (var i = 1; i < rawData.length; i++) {
            ps_data[rawData[i].name] = rawData[i];
            categoryData.push(rawData[i].name);
            threeAgoLPriceData.push(rawData[i].threeAgoLPrice.toFixed(2));
            threeAgoHPriceData.push(rawData[i].threeAgoHPrice.toFixed(2));
            predictLPriceData.push(rawData[i].predicLPrice.toFixed(2));
            predictHPriceData.push(rawData[i].predicHPrice.toFixed(2));
        }
        categoryArr = categoryData;
        threeAgoWhiteBar = threeAgoLPriceData;
        threeAgoHPriceBar = threeAgoHPriceData;
        predictWhiteBar = predictLPriceData;
        predictHPriceBar = predictHPriceData;
    }

    function psDraw() {
        var ps_option = {
            /* backgroundColor: '#142058', */

            title: {
                text: ps_area + '\n蔬菜价格态势波动',
                x: 'right',
                align: 'right'
            },
            grid: {},
            tooltip: {
                trigger: 'item',
                formatter: function (param, ticket, html) {
                    var itemObj = ps_data[param.name];
                    var threeAgoLPrice = itemObj.threeAgoLPrice.toFixed(2);
                    var threeAgoHPrice = itemObj.threeAgoHPrice.toFixed(2);
                    var predictHPrice = itemObj.predicHPrice.toFixed(2);
                    var predictLPrice = itemObj.predicLPrice.toFixed(2);
                    var str = param.name + "<br>" + "前三天最低价平均值:"
                        + threeAgoLPrice + "<br>" + "前三天最高价平均值:"
                        + threeAgoHPrice + "<br>" + "预测最低价:"
                        + predictLPrice + "<br>" + "预测最高价:"
                        + predictHPrice;
                    return str;
                }
            },
            yAxis: [{
                name: '蔬菜种类',
                axisLine: {
                    show: true,
                    symbol: ['none', 'arrow'],
                    symbolOffset: [0, 10]
                },
                splitLine: {
                    show: true,
                    lineStyle: {
                        color: ['#000']
                    }
                },
                type: "category",
                data: categoryArr
            }],
            xAxis: [{
                name: '价格(单位：元)',
                axisLine: {
                    show: true,
                    symbol: ['none', 'arrow'],
                    symbolOffset: [0, 10]
                },
                splitLine: {
                    show: false
                },
                type: 'value',

            }],
            legend: {
                data: lengendArr,
                x: 'left'
            },
            series: [{
                name: "前三天价格最低平均值",
                type: "bar",
                stack: "前三天",
                barWidth: 10,
                itemStyle: {
                    normal: {
                        barBorderColor: 'rgba(222,222,0,0)',
                        color: 'rgba(0,0,0,0)'
                    }
                },
                data: threeAgoWhiteBar
            }, {
                name: "前三天价格范围",
                type: "bar",
                barWidth: 10,
                stack: "前三天",
                data: threeAgoHPriceBar
            }, {
                name: "预测价格最低平均值",
                type: "bar",
                stack: "预测",
                barWidth: 10,
                itemStyle: {
                    normal: {
                        barBorderColor: 'rgba(222,222,0,0)',
                        color: 'rgba(0,0,0,0)'
                    }
                },
                data: predictWhiteBar
            }, {
                name: "预测价格范围",
                type: "bar",
                barWidth: 10,
                stack: "预测",

                data: predictHPriceBar
            },]
        };

        ;
        if (ps_option && typeof ps_option === "object") {
            ps_chart.setOption(ps_option, true);
        }
    }

//初始化蔬菜种类列表
    function ps_updateItems() {

        ps_initdata();

    };

    ps_chart.on('click', function (params) {
        console.log(params.name);
        parent.location.href = "sucai-dapin?name=" + params.name + "&area="
            + ps_area;
    });
}

<!-- 近期最高价上涨趋势最大的五种蔬菜  -->
{
    var hs_chart = echarts.init(document.getElementById("high-situation"));
    var hs_data;//蔬菜趋势排名
    var hs_name;
    $.get(
        "sys/vegetable/tendencyhpricelist?days=" + ps_days + "&area="
        + ps_area,

        function (r) {
            hs_data = r;
            hs_name = hsSplitNameData(hs_data);

            hsDraw();
        });

    function hsSplitNameData(raw) {
        var name = [];
        for (var i = 0; i < 5; i++) {
            var n = raw[i][0].name;
            name.push(n);
        }
        return name;
    }

    function hsSplitPriceData(key) {
        var result = [];
        i = 0;
        j = 0;

        for (i = 0; i < stime.length, j < hs_data[key].length; i++) {

            if (hs_data[key][j].time = stime[i]) {
                result.push(hs_data[key][j].hPrice);
                j++;
                continue;
            } else if (hs_data[key][j].time > stime[i]) {
                continue;
            }

        }
        return result;
    }

    function hsDraw() {
        var hs_option = {
            title: {
                text: '近期最高价上涨趋势\n最大的五种蔬菜',
                textStyle: {
                    fontSize: 16,
                    color: '#000000',
                }
            },
            tooltip: {
                trigger: 'axis'
            },
            legend: {
                data: hs_name,
                x: 'right',
            },
            grid: {
                left: '3%',
                right: '4%',
                bottom: '3%',
                containLabel: true
            },
            /*toolbox: {
                feature: {
                    saveAsImage: {}
                }
            },*/
            xAxis: {
                type: 'category',
                boundaryGap: false,
                data: stime
            },
            yAxis: {
                type: 'value'
            },
            dataZoom: [{
                type: 'inside',
                start: 0,
                end: 50
            }, {
                show: true,
                type: 'slider',
                y: '90%',
                start: 50,
                end: 100
            }],
            series: [{
                name: hs_name[0],
                type: 'line',
                stack: '总量',
                smooth: true,
                data: hsSplitPriceData(0)
            }, {
                name: hs_name[1],
                type: 'line',
                stack: '总量',
                data: hsSplitPriceData(1)
            }, {
                name: hs_name[2],
                type: 'line',
                stack: '总量',
                data: hsSplitPriceData(2)
            }, {
                name: hs_name[3],
                type: 'line',
                stack: '总量',
                data: hsSplitPriceData(3)
            }, {
                name: hs_name[4],
                type: 'line',
                stack: '总量',
                data: hsSplitPriceData(4)
            }]
        };
        ;
        if (hs_option && typeof hs_option === "object") {
            hs_chart.setOption(hs_option, true);
        }
    }
}
<!-- 近期最低价下降趋势最大的五种蔬菜  -->
{
    var ls_chart = echarts.init(document.getElementById("low-situation"));
    var ls_data;
    var ls_name
    $.get(
        "sys/vegetable/tendencylpricelist?days=" + ps_days + "&area="
        + ps_area,

        function (r) {
            ls_data = r;
            ls_name = lsSplitNameData(ls_data);
            lsDraw();
        });
    function lsSplitNameData(raw) {
        var name = [];
        for (var i = (raw.length - 1); i > (raw.length - 6) && i > 0; i--) {
            var n = raw[i][0].name;
            name.push(n);
        }
        return name;
    }
    function lsSplitPriceData(key) {
        var result = [];
        i = 0;
        j = 0;
        key = ls_data.length - 1 - key;
        for (i = 0; i < stime.length, j < ls_data[key].length; i++) {

            if (ls_data[key][j].time = stime[i]) {
                result.push(ls_data[key][j].lPrice);
                j++;
                continue;
            } else if (ls_data[key][j].time > stime[i]) {
                continue;
            }

        }
        return result;
    }
    function lsDraw() {
        var ls_option = {
            title: {
                text: '近期最低价下降趋势\n最大的五种蔬菜',
                textStyle: {
                    fontSize: 16,
                    color: '#000000',
                }
            },
            tooltip: {
                trigger: 'axis'
            },
            legend: {
                data: ls_name,
                x: 'right',
            },
            grid: {
                left: '3%',
                right: '4%',
                bottom: '3%',
                containLabel: true
            },
            /*toolbox: {
                feature: {
                    saveAsImage: {}
                }
            },*/
            dataZoom: [{
                type: 'inside',
                start: 0,
                end: 50
            }, {
                show: true,
                type: 'slider',
                y: '90%',
                start: 50,
                end: 100
            }],
            xAxis: {
                type: 'category',
                boundaryGap: false,
                data: stime
            },
            yAxis: {
                type: 'value'
            },
            series: [{
                name: ls_name[0],
                type: 'line',
                stack: '总量',
                data: lsSplitPriceData(0)
            }, {
                name: ls_name[1],
                type: 'line',
                stack: '总量',
                data: lsSplitPriceData(1)
            }, {
                name: ls_name[2],
                type: 'line',
                stack: '总量',
                data: lsSplitPriceData(2)
            }, {
                name: ls_name[3],
                type: 'line',
                stack: '总量',
                data: lsSplitPriceData(3)
            }, {
                name: ls_name[4],
                type: 'line',
                stack: '总量',
                data: lsSplitPriceData(4)
            }]
        };
        ;
        if (ls_option && typeof ls_option === "object") {
            ls_chart.setOption(ls_option, true);
        }
    }
}