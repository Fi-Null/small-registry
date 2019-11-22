<!DOCTYPE html>
<html>
<head>
	<#import "./common/common.macro.ftl" as netCommon>
  	<title>分布式服务注册中心</title>
	<@netCommon.commonStyle />
</head>
<body class="hold-transition skin-blue sidebar-mini <#if cookieMap?exists && cookieMap["registry_adminlte_settings"]?exists && "off" == cookieMap["xxlregistry_adminlte_settings"].value >sidebar-collapse</#if> ">
<div class="wrapper">
	<!-- header -->
	<@netCommon.commonHeader />
	<!-- left -->
	<@netCommon.commonLeft "help" />
	
	<!-- Content Wrapper. Contains page content -->
	<div class="content-wrapper">
		<!-- Content Header (Page header) -->
		<section class="content-header">
			<h1>使用教程<small></small></h1>
		</section>

		<!-- Main content -->
		<section class="content">
			<div class="callout callout-info">
				<h4>分布式服务注册中心XXL-REGISTRY</h4>
				<br>
				<p>
                   <#-- <a target="_blank" href="https://github.com/xuxueli/xxl-registry">Github</a>&nbsp;&nbsp;&nbsp;&nbsp;
                    <iframe src="https://ghbtns.com/github-btn.html?user=xuxueli&repo=xxl-registry&type=star&count=true" frameborder="0" scrolling="0" width="170px" height="20px" style="margin-bottom:-5px;"></iframe>
                    <br><br>
                    <a target="_blank" href="https://www.xuxueli.com/xxl-registry/">官方文档</a>
                    <br><br>-->

				</p>
				<p></p>
            </div>
		</section>
		<!-- /.content -->
	</div>
	<!-- /.content-wrapper -->
	
	<!-- footer -->
	<@netCommon.commonFooter />
</div>
<@netCommon.commonScript />
</body>
</html>
