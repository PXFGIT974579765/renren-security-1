
var gridTaleIds=[];
var vm = new Vue({
	el:'#rrapp',
	data:{
		q:{
			beanName: null
		},
		showList: true,
		showMode0:true,
		showMode1:false,
		showMode2:false,
		title: null,
		schedule:{
            type: 0,
            blockJobIds:'',
			mode:0,
            needQueryFlag:0
		},
        multiple: {
            jobList: [],
            selectedJobList: []
        },
        flag:null,
        typeOptions:[
            { text: '本地任务', value: '0' },
            { text: '远程任务', value: '1' },
		],
		modeOptions:[
			{text:'类名方法名参数名',value:0},
			{text:'restful-api',value:1},
			{text:'shell脚本',value:2}
		]
	},
	methods: {
		query: function () {
			vm.reload();
		},
        modeChangeCallback:function(e){
			debugger;
			var mode=e.target.value||" ";
            vm.switchMode(mode);

		},
		switchMode:function(mode){
            if(mode==0){
                vm.showMode0=true;
                vm.showMode1=false;
                vm.showMode2=false;
            }else if(mode==1){
                vm.showMode0=false;
                vm.showMode1=true;
                vm.showMode2=false;
            }else if(mode==2){
                vm.showMode0=false;
                vm.showMode1=false;
                vm.showMode2=true;
            }
		},
        typeChangeCallback:function(e){
		    debugger;
			var type=e.target.value;
            vm.switchType(type);

		},
		switchType:function(type,mode){
		    debugger;
            if(type==1){
                vm.modeOptions=[];
                vm.modeOptions.push({text:'restful-api',value:1},{text:'shell脚本',value:2});
                if(mode){
                    vm.schedule.mode=mode;
                }else{
                    vm.schedule.mode=1;
                    vm.showMode0=false;
                    vm.showMode1=true;
                    vm.showMode2=false;
                }
            }else{
                vm.modeOptions=[];
                vm.modeOptions.push({text:'类名方法名参数名',value:0},{text:'restful-api',value:1},{text:'shell脚本',value:2});
                if(mode){
                    vm.schedule.mode=mode;
                }else{
                    vm.schedule.mode=0;
                    vm.showMode0=true;
                    vm.showMode1=false;
                    vm.showMode2=false;
                }
            }
		},
        multipleCallback: function(data){
			//console.log(this.$ref.selectedIdList);
            this.multiple.selectedJobList = data;
           console.log('父级元素调用multipleSelected 选中的是' + JSON.stringify(data))
        },
        getBlockJobIds:function(list){
			debugger;
			var ids=list.join(',')
            vm.schedule.blockJobIds=ids;
		},
        add: function () {
		    vm.flag="add";
            vm.showList = false;
            vm.title = "新增";
            vm.schedule = {
                type: 0,
                blockJobIds: '',
                mode: 0,
                needQueryFlag: 0
            };
            this.multiple.selectedJobList=[];
            $.get(baseURL + "sys/schedule/all", function (r) {
                vm.multiple.jobList = r.list;
            });
        },
		update: function () {
		    vm.flag="update";
			debugger;
			var jobId = getSelectedRow(gridTaleIds);
			if(jobId == null){
				return ;
			}
			
			$.get(baseURL + "sys/schedule/info/"+jobId, function(r){
				debugger;
				vm.showList = false;
                vm.title = "修改";
				vm.schedule = r.schedule;
				debugger;
				console.log(vm.schedule);
                vm.switchMode(vm.schedule.mode);
                vm.switchType(vm.schedule.type,vm.schedule.mode);
                $.get(baseURL + "sys/schedule/all", function(rs){
                    vm.multiple.jobList = rs.list;
                    var selectedList=[];
                    var blockIds=(r.schedule.blockJobIds||" ").split(",");
                    for (var i in rs.list){
                    	if(blockIds.indexOf(rs.list[i].jobId)!=-1){
                    		selectedList.push(rs.list[i]);
						}
					}
                    vm.multiple.selectedJobList = selectedList;
                });
				console.log(JSON.stringify(vm.schedule));
			});
		},
		detail:function(){
            vm.update();
            $(":input").attr("disabled","disabled");
            $("#backBtn").attr("disabled",null);
        },
		saveOrUpdate: function (obj) {
			var url = vm.schedule.jobId == null ? "sys/schedule/save" : "sys/schedule/update";
			$.ajax({
				type: "POST",
			    url: baseURL + url,
                contentType: "application/json",
			    data: JSON.stringify(vm.schedule),
			    success: function(r){
			    	if(r.code === 0){
						alert('操作成功', function(index){
							vm.reload();
						});
					}else{
						alert(r.msg);
					}
				}
			});
		},
		del: function (event) {
			var jobIds = getSelectedRows(gridTaleIds);
			if(jobIds == null){
				return ;
			}
			
			confirm('确定要删除选中的记录？', function(){
				$.ajax({
					type: "POST",
				    url: baseURL + "sys/schedule/delete",
                    contentType: "application/json",
				    data: JSON.stringify(jobIds),
				    success: function(r){
						if(r.code == 0){
							alert('操作成功', function(index){
								vm.reload();
							});
						}else{
							alert(r.msg);
						}
					}
				});
			});
		},
		pause: function (event) {
            var jobIds = getSelectedRows(gridTaleIds);
			if(jobIds == null){
				return ;
			}
			
			confirm('确定要暂停选中的记录？', function(){
				$.ajax({
					type: "POST",
				    url: baseURL + "sys/schedule/pause",
                    contentType: "application/json",
				    data: JSON.stringify(jobIds),
				    success: function(r){
						if(r.code == 0){
							alert('操作成功', function(index){
								vm.reload();
							});
						}else{
							alert(r.msg);
						}
					}
				});
			});
		},
		resume: function (event) {
            var jobIds = getSelectedRows(gridTaleIds);
			if(jobIds == null){
				return ;
			}
			
			confirm('确定要恢复选中的记录？', function(){
				$.ajax({
					type: "POST",
				    url: baseURL + "sys/schedule/resume",
                    contentType: "application/json",
				    data: JSON.stringify(jobIds),
				    success: function(r){
						if(r.code == 0){
							alert('操作成功', function(index){
								vm.reload();
							});
						}else{
							alert(r.msg);
						}
					}
				});
			});
		},
		runOnce: function (event) {
            var jobIds = getSelectedRows(gridTaleIds);
			if(jobIds == null){
				return ;
			}
			
			confirm('确定要立即执行选中的记录？', function(){
				$.ajax({
					type: "POST",
				    url: baseURL + "sys/schedule/run",
                    contentType: "application/json",
				    data: JSON.stringify(jobIds),
				    success: function(r){
						if(r.code == 0){
							alert('操作成功', function(index){
								vm.reload();
							});
						}else{
							alert(r.msg);
						}
					}
				});
			});
		},
        queryOnce: function (event) {
            var jobIds = getSelectedRows(gridTaleIds);
            if (jobIds == null) {
                return;
            }

            $.ajax({
                type: "POST",
                url: baseURL + "sys/schedule/pullstate",
                contentType: "application/json",
                data: JSON.stringify(jobIds),
                success: function (r) {
                    if (r.code == 0) {
                        alert('操作成功', function (index) {
                            vm.reload();
                        });
                    } else {
                        alert(r.msg);
                    }
                }
            });
        },
		reload: function (event) {
            $(":input").attr("disabled",null);
			vm.showList = true;
			var page = $("#jqGrid_table").jqGrid('getGridParam','page');
			$("#jqGrid_table").jqGrid('setGridParam',{
                postData:{'beanName': vm.q.beanName},
                page:page 
            }).trigger("reloadGrid");
		}
	}
});
vm.$watch('schedule.mode',function (newValue,oldValue) {
    vm.switchMode(newValue);
});
vm.$watch('schedule.type',function (newValue,oldValue) {
    debugger;
    if(vm.flag=='add'){
        vm.switchType(newValue);
    }else{
        vm.switchType(newValue,vm.schedule.mode);
    }
})


$(function () {

    gridTaleIds.push('jqGrid_table');
    $("#jqGrid_table").jqGrid({
        url: baseURL + 'sys/schedule/list',
        datatype: "json",
        colModel: [
            { label: '任务ID', name: 'jobId', width: 60, key: true },
            { label: '任务名称', name: 'jobName', width: 100 },
            { label: '任务类型', name: 'type', width: 100,formatter:function(cellValue){return cellValue==0?'本地任务':'远程任务';} },
            { label: '调度方式', name: 'mode', width: 100,formatter:function(cellValue){return cellValue==0?'类方法参数':cellValue==1?'rest api':'shell脚本';} },
            { label: '是否需异步查询结果', name: 'needQueryFlag', width: 100,formatter:function(cellValue){return cellValue==0?'不需要':'需要';} },
            //{ label: '依赖于其他任务', name: 'blockJobIds', width: 100,formatter:function(cellValue){return cellValue==null?'无依赖':'有依赖';} },
            { label: '状态', name: 'state', width: 100,formatter:function(cellValue){
                 var msg;
                  if(cellValue==1){
                  	msg="被阻塞";
				  }else if(cellValue==2){
                  	msg="调度成功";
				  }else if(cellValue==3){
                  	msg="调度失败";
				  }else if(cellValue==4){
                  	msg="执行成功";
				  }else if(cellValue==5){
                  	msg="执行失败";
				  }else{
                      msg="无状态";
				  }
				  return msg;
            }
            },
            // { label: 'bean名称', name: 'beanName', width: 100 },
            // { label: '方法名称', name: 'methodName', width: 100 },
            // { label: '参数', name: 'params', width: 100 },
            { label: 'cron表达式 ', name: 'cronExpression', width: 100 },
            { label: '备注 ', name: 'remark', width: 100 },
            { label: '开启状态', name: 'status', width: 60, formatter: function(value, options, row){
                    return value === 0 ?
                        '<span class="label label-success">正常</span>' :
                        '<span class="label label-danger">暂停</span>';
                }}
        ],
        viewrecords: true,
        height: 385,
        rowNum: 10,
        rowList : [10,30,50],
        rownumbers: true,
        rownumWidth: 25,
        autowidth:true,
        subGrid : true,

        multiselect: true,
        pager: "#jqGridPager",
        jsonReader : {
            root: "page.list",
            page: "page.currPage",
            total: "page.totalPage",
            records: "page.totalCount",
            subgrid: {
                root: "page.list"
            }
        },
        prmNames : {
            page:"page",
            rows:"limit",
            order: "order"
        },
        gridComplete:function(){
            //隐藏grid底部滚动条
            $("#jqGrid").closest(".ui-jqgrid-bdiv").css({ "overflow-x" : "hidden" });
        },
        subGridRowExpanded: function(subgrid_id, row_id) {  // (2)子表格容器的id和需要展开子表格的行id，将传入此事件函数
            var subgrid_table_id;
            subgrid_table_id = "jqGrid_table"+subgrid_id ;   // (3)根据subgrid_id定义对应的子表格的table的id
            gridTaleIds.push(subgrid_table_id);
            var subgrid_pager_id;
            subgrid_pager_id = "jqp"+subgrid_id   // (4)根据subgrid_id定义对应的子表格的pager的id

            // (5)动态添加子报表的table和pager
            $("#" + subgrid_id).html("<table id='"+subgrid_table_id+"' class='scroll'></table><div id='"+subgrid_pager_id+"' class='scroll'></div>");

            // (6)创建jqGrid对象
            $("#" + subgrid_table_id).jqGrid({
                url: baseURL + 'sys/schedule/child/'+row_id,  // (7)子表格数据对应的url，注意传入的contact.id参数
                datatype: "json",
                colModel: [
                    { label: '任务ID', name: 'jobId', width: 60, key: true },
                    { label: '任务名称', name: 'jobName', width: 100 },
                    { label: '任务类型', name: 'type', width: 100,formatter:function(cellValue){return cellValue==0?'本地任务':'远程任务';} },
                    { label: '调度方式', name: 'mode', width: 100,formatter:function(cellValue){return cellValue==0?'类方法参数':cellValue==1?'rest api':'shell脚本';} },
                    { label: '是否需异步查询结果', name: 'needQueryFlag', width: 100,formatter:function(cellValue){return cellValue==0?'不需要':'需要';} },
                    { label: '所依赖的任务id', name: 'parent', width: 100 },
                    { label: '状态', name: 'state', width: 100,formatter:function(cellValue){
                            var msg;
                            if(cellValue==1){
                                msg="被阻塞";
                            }else if(cellValue==2){
                                msg="调度成功";
                            }else if(cellValue==3){
                                msg="调度失败";
                            }else if(cellValue==4){
                                msg="执行成功";
                            }else if(cellValue==5){
                                msg="执行失败";
                            }else{
                                msg="无状态";
                            }
                            return msg;
                        }
                    },
                    { label: '备注 ', name: 'remark', width: 100 },
                    { label: '开启状态', name: 'status', width: 60, formatter: function(value, options, row){
                            return value === 0 ?
                                '<span class="label label-success">正常</span>' :
                                '<span class="label label-danger">暂停</span>';
                        }}
                ],
                gridComplete:function(){
                    //隐藏grid底部滚动条
                    $("#" + subgrid_table_id).closest(".ui-jqgrid-bdiv").css({ "overflow-x" : "hidden" });
                },
                jsonReader : {
                    root: "page.list",
                    page: "page.currPage",
                    total: "page.totalPage",
                    records: "page.totalCount",
                    subgrid: {
                        root: "page.list"
                    }
                },
                multiselect: true,
                prmNames: {search: "search"},
                pager: subgrid_pager_id,
                viewrecords: true,
                height: "100%",
                rowNum: 5
            });
        },
    });

});