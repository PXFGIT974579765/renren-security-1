

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
			var mode=e.target.value;
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
			var type=e.target.value;
			if(type==1){
				vm.modeOptions=[];
				vm.schedule.mode=1;
                vm.showMode0=false;
                vm.showMode1=true;
                vm.showMode2=false;
				vm.modeOptions.push({text:'restful-api',value:1},{text:'shell脚本',value:2});
			}else{
                vm.modeOptions=[];
                vm.modeOptions.push({text:'类名方法名参数名',value:0},{text:'restful-api',value:1},{text:'shell脚本',value:2});
                vm.schedule.mode=0;
                vm.showMode0=true;
                vm.showMode1=false;
                vm.showMode2=false;
			}
		},
        multipleCallback: function(data){
			//console.log(this.$ref.selectedIdList);
            this.multiple.selectedJobList = data;
           console.log('父级元素调用multipleSelected 选中的是' + JSON.stringify(data))
        },
        getBlockJobIds:function(list){
			var ids=list.join(',')
            vm.schedule.blockJobIds=ids;
		},
		add: function(){
			vm.showList = false;
			vm.title = "新增";
			vm.schedule = {
                type: 0,
                blockJobIds:'',
                mode:0,
                needQueryFlag:0
            };
            $.get(baseURL + "sys/schedule/all", function(r){
                vm.multiple.jobList = r.list;
            });
		},
		update: function () {
			var jobId = getSelectedRow();
			if(jobId == null){
				return ;
			}
			
			$.get(baseURL + "sys/schedule/info/"+jobId, function(r){
				vm.showList = false;
                vm.title = "修改";
				vm.schedule = r.schedule;
			});
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
			var jobIds = getSelectedRows();
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
			var jobIds = getSelectedRows();
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
			var jobIds = getSelectedRows();
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
			var jobIds = getSelectedRows();
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
		reload: function (event) {
			vm.showList = true;
			var page = $("#jqGrid").jqGrid('getGridParam','page');
			$("#jqGrid").jqGrid('setGridParam',{ 
                postData:{'beanName': vm.q.beanName},
                page:page 
            }).trigger("reloadGrid");
		}
	}
});


$(function () {
    $("#jqGrid").jqGrid({
        url: baseURL + 'sys/schedule/list',
        datatype: "json",
        colModel: [
            { label: '任务ID', name: 'jobId', width: 60, key: true },
            { label: '任务名称', name: 'jobName', width: 100 },
            { label: 'bean名称', name: 'beanName', width: 100 },
            { label: '方法名称', name: 'methodName', width: 100 },
            { label: '参数', name: 'params', width: 100 },
            { label: 'cron表达式 ', name: 'cronExpression', width: 100 },
            { label: '备注 ', name: 'remark', width: 100 },
            { label: '状态', name: 'status', width: 60, formatter: function(value, options, row){
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
        multiselect: true,
        pager: "#jqGridPager",
        jsonReader : {
            root: "page.list",
            page: "page.currPage",
            total: "page.totalPage",
            records: "page.totalCount"
        },
        prmNames : {
            page:"page",
            rows:"limit",
            order: "order"
        },
        gridComplete:function(){
            //隐藏grid底部滚动条
            $("#jqGrid").closest(".ui-jqgrid-bdiv").css({ "overflow-x" : "hidden" });
        }
    });

});