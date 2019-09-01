package DateBaseTools;

import javax.xml.stream.Location;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
*
*	动态生成java类和配置文件
*/
public class MySqlCreatePojoAndDao {

	private Connection conn=null;
	private String url = "jdbc:mysql://127.0.0.1:3306/credit_application?useUnicode=true&characterEncoding=utf-8";
	private String userName ="root";
	private String userPwd="ghw";
	private PreparedStatement ps= null;
	private ResultSet rs =null;
	private ResultSetMetaData rm =null;
	private int isAllTable =0;
	private ArrayList<String> tables=new ArrayList<String>();//生成的表名
	private String projectName = "";
	private String projectControllerServiceName = "";

	private String pojopack="cn.bsit.system."+projectName+"pojo";
	private String daopack ="cn.bsit.system."+projectName+"dao";
	private String daoimplpack ="boer.cims.core.daoimpl";
	private String servicepack = "cn.bsit.system."+projectName+"service";
	private String serviceimplpack = "cn.bsit.system."+projectName+"service.impl";
	private String controllerpack = "cn.bsit.system."+projectName+"controller";

	private String filePath="c:/userfiles/";	//生成到文件路径

	//连接数据库
	public MySqlCreatePojoAndDao() throws Exception{
			Class.forName("com.mysql.jdbc.Driver");
			conn =DriverManager.getConnection(url, userName, userPwd);

			isAllTable =1;//0查询库中所有用户表 1指定表名

			//需要生成的表

		tables.add("merchant");
//		tables.add("ba_work_shop");
//		tables.add("ba_shift");
//		tables.add("ba_product_cfg");
//		tables.add("ba_product_cfg_type");
//		tables.add("ba_product_cfg_detail");

	}

	//获取库中所有用户表
	private ArrayList<String> getDataTables() throws Exception{
		ArrayList<String> tmpList =new ArrayList<String>();
		String sql ="select table_name FROM information_schema.tables where table_schema='credit_application'";
		conn =getConn();
		ps= conn.prepareStatement(sql);
		rs =ps.executeQuery();
		while(rs.next()){
			tmpList.add(rs.getString(1));
		}
		this.closeConn(conn, ps, rs);
		return tmpList;
	}
	private Connection getConn() throws Exception{
		if(conn == null){
			conn =DriverManager.getConnection(url, userName, userPwd);
		}
		return conn;
	}
	private void closeConn(Connection con,PreparedStatement ps,ResultSet rs) throws Exception{
		if (rs!= null) {
			rs.close();
			rs=null;
		}
		if (ps!= null) {
			ps.close();
			ps=null;
		}
		if (con!= null) {
			con.close();
			conn = null;
		}
	}
	private void getTablePojo() throws Exception{
		if (isAllTable == 0) {
			tables=this.getDataTables();
		}
		for (int i = 0; i < tables.size(); i++) {
			//String sql ="select * from "+tables.get(i);
			String sql ="show full columns from "+tables.get(i);
			String reString =this.getTableString(sql,tables.get(i));
			//写入文件
			SaveFile.writeFile(filePath+"/pojo/"+getTableOrColumn(tables.get(i), 1)+".java", reString);
		}
	}
	
	private void getTableDao() throws Exception{
		if (isAllTable == 0) {
			tables=this.getDataTables();
		}
		for (int i = 0; i < tables.size(); i++) {
			String sql ="select * from "+tables.get(i);
			String reString =this.getTableDaoString(sql,tables.get(i));
			//写入文件
			SaveFile.writeFile(filePath+"/dao/"+getTableOrColumn(tables.get(i), 1)+"Dao.java", reString);
		}
	}

	private void getTableIService() throws Exception{
		if (isAllTable == 0) {
			tables=this.getDataTables();
		}
		for (int i = 0; i < tables.size(); i++) {
			String sql ="select * from "+tables.get(i);
			String reString =this.getTableIServiceString(sql,tables.get(i));
			//写入文件
			SaveFile.writeFile(filePath+"/iservice/I"+getTableOrColumn(tables.get(i), 1)+"Service.java", reString);
		}
	}

	//getTableServiceImpl
	private void getTableServiceImpl() throws Exception{
		if (isAllTable == 0) {
			tables=this.getDataTables();
		}
		for (int i = 0; i < tables.size(); i++) {
			String sql ="select * from "+tables.get(i);
			String reString =this.getTableServiceImplString(sql,tables.get(i));
			//写入文件
			SaveFile.writeFile(filePath+"/serviceimpl/"+getTableOrColumn(tables.get(i), 1)+"ServiceImpl.java", reString);
		}
	}

	//getTableController
	private void getTableController() throws Exception{
		if (isAllTable == 0) {
			tables=this.getDataTables();
		}
		for (int i = 0; i < tables.size(); i++) {
			String sql ="select * from "+tables.get(i);
			String reString =this.getTableControllerString(sql,tables.get(i));
			//写入文件
			SaveFile.writeFile(filePath+"/controller/"+getTableOrColumn(tables.get(i), 1)+"Controller.java", reString);
		}
	}

	private void getTableDaoImpl() throws Exception{
		if (isAllTable == 0) {
			tables=this.getDataTables();
		}
		for (int i = 0; i < tables.size(); i++) {
			String sql ="select * from "+tables.get(i);
			String reString =this.getTableDaoImplString(sql,tables.get(i));
			//写入文件
			SaveFile.writeFile(filePath+"/daoimpl/"+getTableOrColumn(tables.get(i), 1)+"DaoImpl.java", reString);
		}
	}
	
	private String getTableDaoImplString(String sql,String tableName) throws Exception{
		StringBuffer sb =new StringBuffer();
		String tname= getTableOrColumn(tableName, 1);
		sb.append("package "+daoimplpack+";\n\n");
		sb.append("import "+pojopack+".*;\n");
		sb.append("import "+daopack+".*;\n");
		sb.append("/**\n");
		sb.append(" * 表："+tableName+" 对应daoImpl\n");
		sb.append(" */\n");
		conn=getConn();
		ps= conn.prepareStatement(sql);
		rs= ps.executeQuery();
		rm=rs.getMetaData();
		String tmp="";
		if (rm.getColumnCount()>0) {
			int type =rm.getColumnType(1);
			if (type==Types.INTEGER) {
				tmp="Long";
			}else{
				tmp ="String";
			}
		}
		sb.append("public class "+tname+"DaoImpl extends BaseHapiDaoimpl<"+tname+", "+tmp+"> implements I"+tname+"Dao {\n\n");
		sb.append("   public "+tname+"DaoImpl(){\n");
		sb.append("      super("+tname+".class);\n");
		sb.append("   }\n");
		sb.append("}");
		this.closeConn(conn, ps, rs);
		return sb.toString();
	}
	
	private String getTableDaoString(String sql,String tableName) throws Exception{
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
		String nowDate = df.format(new Date());// new Date()为获取当前系统时间
		StringBuffer sb =new StringBuffer();
		String tname= getTableOrColumn(tableName, 1);
		sb.append("package "+daopack+";\n\n");
		sb.append("import cn.bsit.system."+projectName+".api.pojo."+tname+";\n");
		sb.append("import cn.bsit.system.prodplan.api.common.base.dao.BaseDao;\n\n");

		sb.append("/**\n");
		sb.append(" * @Description: 对象持久层，表："+getTableName(tableName)+" 对应dao\n");
		//sb.append(" * @Refrence:\n");
		sb.append(" * \n");
		sb.append(" * @Author: ghw \n");
		sb.append(" * @version: 1.0  " + nowDate + " \n");
		//sb.append(" * @Modify: \n");
		sb.append(" */\n");

		sb.append("public interface "+tname+"Dao extends BaseDao<"+tname+">{\n\n");
		sb.append("}");
		return sb.toString();
	}

	//生成IService接口类
	private String getTableIServiceString(String sql,String tableName) throws Exception{
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
		String nowDate = df.format(new Date());// new Date()为获取当前系统时间
		StringBuffer sb =new StringBuffer();
		String tname= getTableOrColumn(tableName, 1);
		sb.append("package "+servicepack+";\n\n");
		sb.append("import cn.bsit.system."+projectName+".api.pojo."+tname+";\n");
		sb.append("import cn.bsit.system.prodplan.api.common.base.iservice.IBaseService;\n\n");

		sb.append("/**\n");
		sb.append(" * @Description: 业务处理接口，service注入实现。\n");
		//sb.append(" * @Refrence:\n");
		sb.append(" * \n");
		sb.append(" * @Author: ghw \n");
		sb.append(" * @version: 1.0  " + nowDate + " \n");
		//sb.append(" * @Modify: \n");
		sb.append(" */\n");

		sb.append("public interface I"+tname+"Service extends IBaseService<"+tname+">{\n\n");
		sb.append("}");
		return sb.toString();
	}


	//生成IService的实现类
	private String getTableServiceImplString(String sql,String tableName) throws Exception{
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
		String nowDate = df.format(new Date());// new Date()为获取当前系统时间
		StringBuffer sb =new StringBuffer();
		String tname= getTableOrColumn(tableName, 1);
		sb.append("package "+serviceimplpack+";\n\n");
		sb.append("import cn.bsit.system."+projectName+".api.pojo."+tname+";\n");
		sb.append("import cn.bsit.system."+projectName+".api.iservice.I"+tname+"Service;\n");
		sb.append("import cn.bsit.system."+projectName+".service.dao."+tname+"Dao;\n");
		sb.append("import cn.bsit.system.prodplan.api.common.base.serviceimpl.BaseServiceImpl;\n");
		sb.append("import org.springframework.beans.factory.annotation.Autowired;\n");
		sb.append("import org.springframework.stereotype.Service;\n\n");

		sb.append("/**\n");
		sb.append(" * @Description: iservice的实现类，注入dao。\n");
		//sb.append(" * @Refrence:\n");
		sb.append(" * \n");
		sb.append(" * @Author: ghw \n");
		sb.append(" * @version: 1.0  " + nowDate + " \n");
		//sb.append(" * @Modify: \n");
		sb.append(" */\n");

		sb.append("@Service(value = \""+tname+"Service\")\n");
		sb.append("public class "+tname+"ServiceImpl extends BaseServiceImpl<"+tname+"> implements I"+tname+"Service {\n\n");
		sb.append("	@Autowired\n");
		sb.append("	private "+tname+"Dao "+getColumnName(tname)+"Dao;\n");
		sb.append("}");
		return sb.toString();
	}

	//getTableControllerString
	private String getTableControllerString(String sql,String tableName) throws Exception{
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
		String nowDate = df.format(new Date());// new Date()为获取当前系统时间
		StringBuffer sb =new StringBuffer();
		String tname= getTableOrColumn(tableName, 1);
		String controllerName = tableName.replace("_","-").substring(3);
		sb.append("package "+controllerpack+";\n\n");
		sb.append("import cn.bsit.system."+projectName+".api.pojo."+tname+";\n");
		sb.append("import cn.bsit.system."+projectName+".api.iservice.I"+tname+"Service;\n");
		sb.append("import cn.bsit.system.prodplan.api.common.base.controller.BaseController;\n");
		sb.append("import io.swagger.annotations.Api;\n");
		sb.append("import org.springframework.beans.factory.annotation.Autowired;\n");
		sb.append("import org.springframework.web.bind.annotation.RequestMapping;\n");
		sb.append("import org.springframework.web.bind.annotation.RestController;\n\n");

		sb.append("/**\n");
		sb.append(" * @Description: 配置业务Controller\n");
		//sb.append(" * @Refrence:\n");
		sb.append(" * \n");
		sb.append(" * @Author: ghw \n");
		sb.append(" * @version: 1.0  " + nowDate + " \n");
		//sb.append(" * @Modify: \n");
		sb.append(" */\n");

		sb.append("@RestController\n");
		sb.append("@RequestMapping(value = \"/"+projectControllerServiceName+"/"+controllerName+"-service/\")\n");
		sb.append("@Api(\"XXXXXXX服务\")\n");
		sb.append("public class "+tname+"Controller extends BaseController<"+tname+"> {\n\n");

		sb.append("	private I"+tname+"Service "+getColumnName(tname)+"Service;\n");
		sb.append("	@Autowired\n");
		sb.append("	public "+tname+"Controller(I"+tname+"Service "+getColumnName(tname)+"Service) {\n");
		sb.append("		super("+getColumnName(tname)+"Service);\n");
		sb.append("		this."+getColumnName(tname)+"Service = "+getColumnName(tname)+"Service;\n");
		sb.append("	}\n");
		sb.append("}");
		return sb.toString();
	}

	private String getTableString(String sql,String tableName) throws Exception{
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
		String nowDate = df.format(new Date());// new Date()为获取当前系统时间
		StringBuffer sb =new StringBuffer();
		String tname= getTableOrColumn(tableName, 1);
		sb.append("package "+pojopack+";\n\n");
		sb.append("import cn.bsit.system.prodplan.api.common.base.pojo.BaseEntity;\n");
		sb.append("import io.swagger.annotations.ApiParam;\n");
		sb.append("io.swagger.annotations.ApiModelProperty;\n");
		sb.append("import java.util.Date;\n\n");
		sb.append("/**\n");
		sb.append(" * @Description: 实体对象，只和表关联，数据库表名："+getTableName(tableName)+"\n");
		//sb.append(" * @Refrence:\n");
		sb.append(" * \n");
		sb.append(" * @Author: ghw \n");
		sb.append(" * @version: 1.0  " + nowDate + " \n");
		//sb.append(" * @Modify: \n");
		sb.append(" */\n");

		conn=getConn();
		ps= conn.prepareStatement(sql);
		rs= ps.executeQuery();
		//rm=rs.getMetaData();

		sb.append("public class "+tname+" extends BaseEntity {\n\n");
		while (rs.next()){
			String cname=getColumnName(rs.getString("Field"));
			cname = this.alterHumpName(cname);
			if(isExtColumnName(cname)){
				continue;
			}
			String ctype = rs.getString("Type").toLowerCase();
			String ccomment = rs.getString("Comment").trim();
			ccomment = ccomment.replace("\r\n",",").replace(" ","");
			if(!ccomment.equals("")){
				sb.append("    /** "+ccomment+" */\n");
				sb.append("    @ApiModelProperty\n");
				//sb.append("    @ApiParam(\""+ccomment+"\")\n");
			}

			if (ctype.indexOf("int")==0) {
				sb.append("    private Integer "+cname+";\n\n");
			}else if(ctype.indexOf("decimal")==0){
				sb.append("    private  BigDecimal "+cname+";\n");
			}else if(ctype.indexOf("bigint")==0){
				sb.append("    private Long "+cname+";\n\n");
			}else if(ctype.indexOf("datetime")==0){
				sb.append("    private Date "+cname+";\n\n");
			}else{
				sb.append("    private String "+cname+";\n\n");
			}
		}
//		for (int i = 2; i <=rm.getColumnCount(); i++) {
//			//String cname=getTableOrColumn(rm.getColumnName(i), 0);
//			String cname=getColumnName(rm.getColumnName(i));
//
//			if (rm.getColumnType(i) == Types.INTEGER) {
//				sb.append("   private int "+cname+";\n");
//			}else if(rm.getColumnType(i) == Types.NUMERIC ||rm.getColumnType(i)==Types.DECIMAL||rm.getColumnType(i) == Types.DOUBLE || rm.getColumnType(i) == Types.FLOAT){
//				sb.append("   private double "+cname+";\n");
//			}else if(rm.getColumnType(i) == Types.BIGINT){
//				sb.append("   private long "+cname+";\n");
//			}else if(rm.getColumnType(i) == Types.TIMESTAMP){
//				sb.append("   private Date "+cname+";\n");
//			}else{
//				sb.append("   private String "+cname+";\n");
//			}
//		}
//		sb.append("\n");
//		sb.append("   //默认构造方法\n");
//		sb.append("   public "+tname+"(){\n");
//		sb.append("      super();\n");
//		sb.append("   }\n");
//
//		sb.append("\n");
//		sb.append("   //构造方法(手工生成)\n");
//		sb.append("   \n");
//		rs= ps.executeQuery();
//		sb.append("\n");
//		sb.append("    //get和set方法\n");
//		while (rs.next()){
//			String cname=getColumnName(rs.getString("Field"));
//			cname = this.alterHumpName(cname);
//			if(isExtColumnName(cname)){
//				continue;
//			}
//			String ctype = rs.getString("Type").toLowerCase();
//			if (ctype.indexOf("int")==0) {
//				sb.append(this.createGetAndSetMethod("Integer", cname));
//			}else if(ctype.indexOf("decimal")==0){
//				sb.append(this.createGetAndSetMethod("Double", cname));
//			}else if(ctype.indexOf("bigint")==0){
//				sb.append(this.createGetAndSetMethod("Long", cname));
//			}else if(ctype.indexOf("datetime")==0){
//				sb.append(this.createGetAndSetMethod("Date", cname));
//			}else{
//				sb.append(this.createGetAndSetMethod("String", cname));
//			}
//		}

//		for (int i = 2; i <=rm.getColumnCount(); i++) {
//			//String cname=getTableOrColumn(rm.getColumnName(i), 0);
//			String cname=getColumnName(rm.getColumnName(i));
//			if (rm.getColumnType(i) == Types.INTEGER) {
//				sb.append(this.createGetAndSetMethod("int", cname));
//			}else if(rm.getColumnType(i) == Types.NUMERIC || rm.getColumnType(i)==Types.DECIMAL || rm.getColumnType(i) == Types.DOUBLE || rm.getColumnType(i) == Types.FLOAT){
//				sb.append(this.createGetAndSetMethod("double", cname));
//			}else if(rm.getColumnType(i) == Types.BIGINT){
//				sb.append(this.createGetAndSetMethod("long", cname));
//			}else if(rm.getColumnType(i) == Types.TIMESTAMP){
//				sb.append(this.createGetAndSetMethod("Date", cname));
//			}else{
//				sb.append(this.createGetAndSetMethod("String", cname));
//			}
//		}

		sb.append("}");
		this.closeConn(conn, ps, rs);
		return sb.toString();
	}

	private String alterHumpName(String cname) {
		String[] split;
		//将字段更改为驼峰命名的字段
		if(cname.contains("_")){
			split = cname.split("_");
			String str = "";
			for (int i = 0; i < split.length; i++) {
				if(i == 0){
					str += split[i];
				}else{
					split[i] = split[i].substring(0, 1).toUpperCase()+split[i].substring(1);
					str += split[i];
				}
			}
			cname = str;
		}
		return cname;
	}

	private String createGetAndSetMethod(String type,String colsName){
		StringBuffer result =new StringBuffer();
		String tmp1 =colsName.substring(0,1).toUpperCase();
		String tmp2 =colsName.substring(1,colsName.length());
		String tmp3 ="a"+tmp1+tmp2;
		result.append("    public "+type+" get"+tmp1+tmp2+"(){\n");
		result.append("        return "+colsName+";\n");
		result.append("    }\n");

		if("Integer".equals(type)){
			result.append("\n");
			result.append("    public int get"+tmp1+tmp2+"Val(){\n");
			result.append("        return "+colsName+".intValue();\n");
			result.append("    }\n");
		}else if("Double".equals(type)){
			result.append("\n");
			result.append("    public double get"+tmp1+tmp2+"Val(){\n");
			result.append("        return "+colsName+".doubleValue();\n");
			result.append("    }\n");
		}else if("Long".equals(type)){
			result.append("\n");
			result.append("    public long get"+tmp1+tmp2+"Val(){\n");
			result.append("        return "+colsName+".longValue();\n");
			result.append("    }\n");
		}

		result.append("\n");
		result.append("    public void set"+tmp1+tmp2+"("+type+" "+tmp3+"){\n");
		result.append("        this."+colsName+" = "+tmp3+";\n");
		result.append("    }\n");
		result.append("\n");
		return result.toString();
	}
	//对表名和列名进行转换 type==1为表名 否则为列名
	private String getTableOrColumn(String oldname,int type){
		String tmp =oldname.toLowerCase();
		String newStr="";
		if (type == 1) {
			tmp = tmp.substring(3);//10
			String[] tbs =tmp.split("_");
			for (int i = 0; i < tbs.length; i++) {
				String strat=tbs[i].substring(0,1).toUpperCase();
				String end =tbs[i].substring(1,tbs[i].length());
				newStr+=strat+end;
			}
		}else{
			String[] cols =tmp.split("_");
			for (int i = 0; i < cols.length; i++) {
				if (i==0) {
					newStr+=cols[i];
				}else{
					String strat=cols[i].substring(0,1).toUpperCase();
					String end =cols[i].substring(1,cols[i].length());
					newStr+=strat+end;
				}
			}
		}
		return newStr;
		
	}

	//处理列中没有下划线的情况下
	private String getColumnName(String oldname) {
		String newStr = "";
		String strat = oldname.substring(0, 1).toLowerCase();
		String end = oldname.substring(1, oldname.length());
		newStr += strat + end;
		return newStr;
	}

	//特殊列不生成
	private  boolean isExtColumnName(String name){
		if(name.toLowerCase().equals("id")
				||name.toLowerCase().equals("isValid")
				||name.toLowerCase().equals("siteCode")
				||name.toLowerCase().equals("createBy")
				||name.toLowerCase().equals("createTime")
				||name.toLowerCase().equals("updateBy")
				||name.toLowerCase().equals("updateTime")){
			return true;
		}else{
			return false;
		}
	}

	//特殊处理一下表名，去除 除了第一个下划线之后的所有下划线，并将下划线之后的字母大写。比如：ba_key_part_sn_rule，处理后为ba_KeyPartSnRule
	private String getTableName(String oldname) {
		String tmp = oldname.toLowerCase();
		String newStr = "";
		String[] tbs = tmp.split("_");
		for (int i = 0; i < tbs.length; i++) {
			if(i==0){
				String strat = tbs[i].toUpperCase();
				String end = "_";
				newStr += strat + end;
			}else {
				String strat = tbs[i].substring(0, 1).toUpperCase();
				String end = tbs[i].substring(1, tbs[i].length());
				newStr += strat + end;
			}
		}
		if(newStr.endsWith("_")){
			newStr = newStr.substring(0, newStr.length()-1);
		}
		return newStr;
	}

	private void getConfig() throws Exception{
		if (isAllTable == 0) {
			tables=this.getDataTables();
		}
		StringBuffer writeString=new StringBuffer();
		writeString.append("===========复制到dwr_base.xml文件=============\n");
		for (int i = 0; i < tables.size(); i++) {
			String dwrStr =this.getDwrString(tables.get(i));//dwr_base配置文件
			writeString.append(dwrStr);
		}
		writeString.append("==============================================\n\n");
		writeString.append("===========复制到spring-service.xml文件=============\n");
		for (int i = 0; i < tables.size(); i++) {
			String springStr =this.getSpringString(tables.get(i));//spring-service配置文件
			writeString.append(springStr);
		}
		writeString.append("==============================================\n\n");
		writeString.append("===========复制到boer.cims.erp.hbm.xml文件=============\n");
		for (int i = 0; i < tables.size(); i++) {
			String sql ="select * from "+tables.get(i);
			String hibString =this.getHibernateString(sql,tables.get(i));
			writeString.append(hibString);
		}
		writeString.append("==============================================\n\n");
		SaveFile.writeFile(filePath+"config.txt", writeString.toString());
	}

	private void  getMapper() throws Exception {
		if (isAllTable == 0) {
			tables=this.getDataTables();
		}

		for (int i = 0; i < tables.size(); i++) {
			String sql ="show full columns from "+tables.get(i);
			String dwrStr =this.getMapperString(sql,tables.get(i));
			String tname= getTableOrColumn(tables.get(i), 1);
			SaveFile.writeFile(filePath+tname+"Mapper.xml", dwrStr);
		}
	}

	private String getDwrString(String tableName){
		StringBuffer sb =new StringBuffer();
		String tname= getTableOrColumn(tableName, 1);
		sb.append("<convert converter=\"hibernate3\" match=\""+pojopack+"."+tname+"\"/>\n");
		return sb.toString();
	}

	private String getMapperString(String sql, String tableName) throws Exception{
		StringBuffer sb =new StringBuffer();
		String tname= getTableOrColumn(tableName, 1);
		sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n");
		sb.append("<!DOCTYPE mapper PUBLIC \"-//mybatis.org//DTD Mapper 3.0//EN\" \"http://mybatis.org/dtd/mybatis-3-mapper.dtd\">\n");
		sb.append("\n");
		sb.append("<mapper namespace=\"cn.bsit.system."+projectName+".service.dao."+tname+"Dao\">\n");
		sb.append("\n");
		sb.append("    <!-- table name -->\n");
		sb.append("    <sql id=\"tableNameTmp\">\n");
		sb.append("        "+getTableName(tableName) + "\n");
		sb.append("    </sql>\n");
		sb.append("\n");
		sb.append("    <!-- sequence name -->\n");
		sb.append("    <sql id=\"seqNameTmp\">\n");
		sb.append("        S_"+getTableName(tableName) + "\n");
		sb.append("    </sql>\n");
		sb.append("\n");
		sb.append("    <!-- table param -->\n");
		sb.append("    <sql id=\"tableParamTmp\">\n");

		conn=getConn();
		ps= conn.prepareStatement(sql);
		rs= ps.executeQuery();

		String tmp = "        ";
		int count = 0;
		while (rs.next()) {
			String oldColumnName = rs.getString("Field");
			if(!rs.isLast()){
				tmp += oldColumnName+",";
				count++;
				if(count==10)
				{
					count=0;
					tmp+="\n        ";
				}
			}else {
				tmp += oldColumnName;
			}

		}
		sb.append(tmp+"\n");

		sb.append("    </sql>\n");
		sb.append("\n");
		sb.append("    <!-- column mapper -->\n");
		sb.append("    <resultMap id=\"packDataMap\" type=\"cn.bsit.system."+projectName+".api.pojo."+tname+"\">\n");

		rs= ps.executeQuery();
		while (rs.next()) {
			String oldColumnName = rs.getString("Field");
			String cname = getColumnName(oldColumnName);
			if(oldColumnName.toLowerCase().equals("id")){
				cname = cname.toLowerCase();
				sb.append("        <id property=\""+cname+"\" column=\""+oldColumnName+"\"/>\n");
			}else {
				sb.append("        <result property=\""+cname+"\" column=\""+oldColumnName+"\"/>\n");
			}
		}
		sb.append("    </resultMap>\n");
		sb.append("\n");
		sb.append("    <!-- insert param -->\n");
		sb.append("    <sql id=\"insertParamTmp\">\n");

		rs= ps.executeQuery();
		tmp = "        ";
		count=0;
		while (rs.next()) {
			String oldColumnName = rs.getString("Field");
			String cname = getColumnName(oldColumnName);
			if(oldColumnName.toLowerCase().equals("id")){
				cname = cname.toLowerCase();
			}
			if(!rs.isLast()){
				tmp += "#{" + cname+"},";
				count++;
				if(count==10)
				{
					count=0;
					tmp+="\n        ";
				}
			}else {
				tmp += "#{" + cname+"}";
			}

		}
		sb.append(tmp+"\n");
		sb.append("    </sql>\n");

		sb.append("\n");
		sb.append("    <!-- update param -->\n");
		sb.append("    <sql id=\"updateParamTmp\">\n");
		sb.append("        <trim prefix=\"set\" prefixOverrides=\",\">\n");
		rs= ps.executeQuery();
		while (rs.next()) {
			String oldColumnName = rs.getString("Field");
			String cname = getColumnName(oldColumnName);
			if(oldColumnName.toLowerCase().equals("id")
					||oldColumnName.toLowerCase().equals("createuser")
					||oldColumnName.toLowerCase().equals("createdatetime")){
				continue;
			}
			sb.append("            <if test=\""+cname+" != null\">,"+oldColumnName+" = #{"+cname+"}</if>\n");
		}
		sb.append("        </trim>\n");
		sb.append("    </sql>\n");

		sb.append("\n");
		sb.append("    <!-- update properties param(for update by properties) -->\n");
		sb.append("    <sql id=\"updatePropertiesParamTmp\">\n");
		sb.append("        <trim prefix=\"set\" prefixOverrides=\",\">\n");
		rs= ps.executeQuery();
		while (rs.next()) {
			String oldColumnName = rs.getString("Field");
			String cname = getColumnName(oldColumnName);
			if(oldColumnName.toLowerCase().equals("id")
					||oldColumnName.toLowerCase().equals("createuser")
					||oldColumnName.toLowerCase().equals("createdatetime")){
				continue;
			}
			sb.append("            <if test=\"updateMap."+cname+" != null\">,"+oldColumnName+" = #{updateMap."+cname+"}</if>\n");

		}
		sb.append("        </trim>\n");
		sb.append("    </sql>\n");

		sb.append("\n");
		sb.append("    <!-- equals append -->\n");
		sb.append("    <sql id=\"whereAppendPropertiesTmp\">\n");
		rs= ps.executeQuery();
		while (rs.next()) {
			String oldColumnName = rs.getString("Field");
			String cname = getColumnName(oldColumnName);
			String ctype = rs.getString("Type").toLowerCase();
			if(oldColumnName.toLowerCase().equals("id")) {
				cname = cname.toLowerCase();
			}

			if(ctype.indexOf("datetime")==0){
				sb.append("        <if test=\""+cname+" != null\" >\n");
				sb.append("            <if test=\""+cname+"Or == null\" >AND </if>\n");
				sb.append("            <if test=\""+cname+"Or != null\" >OR </if>\n");
				sb.append("            <if test=\""+cname+"DateCompare != null\" >\n");
				sb.append("                <if test=\""+cname+"Between != null\" >\n");
				sb.append("                    "+oldColumnName+" BETWEEN #{"+cname+"Start} and #{"+cname+"End}\n");
				sb.append("                </if>\n");
				sb.append("                <if test=\""+cname+"After != null\" >\n");
				sb.append("                    <![CDATA["+oldColumnName+" >= #{"+cname+"Start}]]>\n");
				sb.append("                </if>\n");
				sb.append("                <if test=\""+cname+"Before != null\" >\n");
				sb.append("                    <![CDATA["+oldColumnName+" <= #{"+cname+"End}]]>\n");
				sb.append("                </if>\n");
				sb.append("            </if>\n");
				sb.append("            <if test=\""+cname+"DateCompare == null\" >\n");
				sb.append("                <if test=\""+cname+"InList != null\" >\n");
				sb.append("                    "+oldColumnName+" <if test=\""+cname+"InListType == false\">NOT</if> IN\n");
				sb.append("                    <foreach collection=\""+cname+"InList\" index=\"idx\" item=\"item\" open=\"(\" separator=\",\" close=\")\">#{item}</foreach>\n");
				sb.append("                </if>\n");
				sb.append("                <if test=\""+cname+"InList == null\" >\n");
				sb.append("                    <if test=\""+cname+"Like == null\" > "+oldColumnName+" = #{"+cname+"}</if>\n");
				sb.append("                    <if test=\""+cname+"Like != null\" > "+oldColumnName+" LIKE CONCAT('%',CONCAT(#{"+cname+"},'%'))</if>\n");
				sb.append("                </if>\n");
				sb.append("            </if>\n");
				sb.append("        </if>\n");
			}else {
				sb.append("        <if test=\""+cname+" != null\" >\n");
				sb.append("            <if test=\""+cname+"Or == null\" >AND </if>\n");
				sb.append("            <if test=\""+cname+"Or != null\" >OR </if>\n");
				sb.append("            <if test=\""+cname+"InList != null\" >\n");
				sb.append("                "+oldColumnName+" <if test=\""+cname+"InListType == false\">NOT</if> IN\n");
				sb.append("                <foreach collection=\""+cname+"InList\" index=\"idx\" item=\"item\" open=\"(\" separator=\",\" close=\")\">#{item}</foreach>\n");
				sb.append("            </if>\n");
				sb.append("            <if test=\""+cname+"InList == null\" >\n");
				sb.append("                <if test=\""+cname+"Like == null\" >"+oldColumnName+" = #{"+cname+"}</if>\n");
				sb.append("                <if test=\""+cname+"Like != null\" >"+oldColumnName+" = LIKE CONCAT('%',CONCAT(#{"+cname+"},'%'))</if>\n");
				sb.append("            </if>\n");
				sb.append("        </if>\n");
			}
		}
		sb.append("    </sql>\n");

		sb.append("\n");
		sb.append("    <!-- ######################## pack function,needn't do anything ########################## -->\n" +
				"\n" +
				"    <!-- static include,make like or equals append sql -->\n" +
				"    <sql id=\"whereAppendTmp\">\n" +
				"        <include refid=\"whereAppendPropertiesTmp\"/>\n" +
				"        <!-- order by -->\n" +
				"        <if test=\"orderByParam != null\">\n" +
				"            ORDER BY #{orderByParam}\n" +
				"            <choose>\n" +
				"                <when test=\"isDesc != null and isDesc == true\">DESC</when>\n" +
				"                <otherwise>ASC</otherwise>\n" +
				"            </choose>\n" +
				"        </if>\n" +
				"    </sql>\n" +
				"    <!-- static include end -->\n" +
				"\n" +
				"    <!--  base method,check parameterType -->\n" +
				"    <insert id=\"insert\">\n" +
				"        <selectKey keyProperty=\"id\" resultType=\"_long\" order=\"BEFORE\">\n" +
				"          select <include refid=\"seqNameTmp\"/>.nextVal as id from dual\n" +
				"        </selectKey>\n" +
				"\n" +
				"        INSERT INTO <include refid=\"tableNameTmp\"/>(<include refid=\"tableParamTmp\"/>)\n" +
				"        VALUES (\n" +
				"          <include refid=\"insertParamTmp\"/>\n" +
				"        )\n" +
				"    </insert>\n" +
				"\n" +
				"    <update id=\"update\">\n" +
				"        UPDATE <include refid=\"tableNameTmp\"/>\n" +
				"          <include refid=\"updateParamTmp\"/>\n" +
				"          where id = #{id}\n" +
				"    </update>\n" +
				"\n" +
				"    <update id=\"updateByProperties\" parameterType=\"Map\">\n" +
				"        UPDATE <include refid=\"tableNameTmp\"/>\n" +
				"        <include refid=\"updatePropertiesParamTmp\"/>\n" +
				"        <where>\n" +
				"            <include refid=\"whereAppendTmp\"/>\n" +
				"        </where>\n" +
				"    </update>\n" +
				"\n" +
				"    <!-- weak delete set invalid -->\n" +
				"    <update id=\"deleteWeaklyById\">\n" +
				"        update <include refid=\"tableNameTmp\"/>\n" +
				"        <![CDATA[\n" +
				"          set isValid = ${validVal} where id = #{id}\n" +
				"        ]]>\n" +
				"    </update>\n" +
				"\n" +
				"    <delete id=\"deleteById\">\n" +
				"        DELETE FROM <include refid=\"tableNameTmp\"/>\n" +
				"        <where>\n" +
				"            <![CDATA[\n" +
				"            AND id = #{id}\n" +
				"            ]]>\n" +
				"        </where>\n" +
				"    </delete>\n" +
				"\n" +
				"    <select id=\"getById\" resultMap=\"packDataMap\">\n" +
				"        SELECT <include refid=\"tableParamTmp\"/> FROM <include refid=\"tableNameTmp\"/>\n" +
				"        <![CDATA[\n" +
				"        where id = #{id}\n" +
				"        ]]>\n" +
				"    </select>\n" +
				"\n" +
				"    <select id=\"list\" resultMap=\"packDataMap\">\n" +
				"        SELECT <include refid=\"tableParamTmp\"/> FROM <include refid=\"tableNameTmp\"/>\n" +
				"    </select>\n" +
				"\n" +
				"    <select id=\"listCount\" resultType=\"java.lang.Integer\">\n" +
				"        SELECT count(id) FROM <include refid=\"tableNameTmp\"/>\n" +
				"    </select>\n" +
				"\n" +
				"    <select id=\"findByProperties\" parameterType=\"map\" resultMap=\"packDataMap\">\n" +
				"        SELECT <include refid=\"tableParamTmp\"/> FROM <include refid=\"tableNameTmp\"/>\n" +
				"        <where>\n" +
				"            <include refid=\"whereAppendTmp\"/>\n" +
				"        </where>\n" +
				"    </select>\n" +
				"\n" +
				"    <select id=\"findByPropertiesCount\" parameterType=\"map\" resultType=\"java.lang.Integer\">\n" +
				"        SELECT count(id) FROM <include refid=\"tableNameTmp\"/>\n" +
				"        <where>\n" +
				"            <include refid=\"whereAppendTmp\"/>\n" +
				"        </where>\n" +
				"    </select>\n" +
				"\n" +
				"\n" +
				"    <select id=\"listByPager\" resultMap=\"packDataMap\">\n" +
				"        SELECT <include refid=\"tableParamTmp\"/> FROM\n" +
				"            (SELECT <include refid=\"tableParamTmp\"/>,ROWNUM RN FROM <include refid=\"tableNameTmp\"/> A\n" +
				"        <![CDATA[\n" +
				"              WHERE ROWNUM <= #{pager.endRow}) B\n" +
				"        WHERE B.RN >= #{pager.startRow}\n" +
				"        ]]>\n" +
				"    </select>\n" +
				"\n" +
				"    <select id=\"findByPropertiesByPager\" parameterType=\"map\" resultMap=\"packDataMap\">\n" +
				"        SELECT <include refid=\"tableParamTmp\"/> FROM\n" +
				"            (SELECT <include refid=\"tableParamTmp\"/>,ROWNUM RN FROM <include refid=\"tableNameTmp\"/> A\n" +
				"                <where>\n" +
				"                    <![CDATA[\n" +
				"                        ROWNUM <= #{pager.endRow}\n" +
				"                    ]]>\n" +
				"                    <include refid=\"whereAppendTmp\"/>\n" +
				"                </where>) B\n" +
				"        <![CDATA[\n" +
				"        WHERE B.RN >= #{pager.startRow}\n" +
				"        ]]>\n" +
				"    </select>\n" +
				"\n" +
				"    <!-- ######################## default function end ########################## -->\n");

		sb.append("\n");
		sb.append("</mapper>\n");
		this.closeConn(conn, ps, rs);
		return sb.toString();
	}
	
	private String getSpringString(String tableName){
		StringBuffer sb= new StringBuffer();
		String tname =getTableOrColumn(tableName, 1);
		sb.append("<bean id=\""+getTableOrColumn(tableName,0)+"DaoImpl\" class=\""+daoimplpack+"."+tname+"DaoImpl\">\n");
		sb.append("    <property name=\"sessionFactory\" ref=\"sessionFactory\"/>\n</bean>\n");
		return sb.toString();
	}
	
	private String getHibernateString(String sql,String tableName) throws Exception{
		StringBuffer sb= new StringBuffer();
		String tname =getTableOrColumn(tableName, 1);
		conn=getConn();
		ps= conn.prepareStatement(sql);
		rs= ps.executeQuery();
		rm=rs.getMetaData();
		String tmp="";
		String tmp2=null;
		String oneCol="";
		if (rm.getColumnCount()>0) {
			oneCol =rm.getColumnName(1);
			int type =rm.getColumnType(1);
			if (type==Types.INTEGER) {
				tmp="long";
			}else{
				tmp ="java.lang.String";
			}
			if (rm.isAutoIncrement(1)) {
				tmp2="        <generator class=\"native\" />";
			}
		}
		sb.append("<class name=\""+pojopack+"."+tname+"\" table=\""+tableName.toLowerCase()+"\">\n");
		sb.append("    <id name=\"primaryKey\" type=\""+tmp+"\">\n");
		sb.append("        <column name=\""+oneCol.toLowerCase()+"\" />\n");
		if (tmp2 != null) {
			sb.append(tmp2+"\n");
		}
		sb.append("    </id>\n");
		for (int i = 2; i <=rm.getColumnCount(); i++) {
			String cname=getTableOrColumn(rm.getColumnName(i), 0);
			if (rm.getColumnType(i) == Types.INTEGER) {
				sb.append(this.CreateHib("java.lang.Integer", cname,rm.getColumnName(i)));
			}else if(rm.getColumnType(i) == Types.NUMERIC ||rm.getColumnType(i) == Types.DECIMAL||rm.getColumnType(i) == Types.DOUBLE || rm.getColumnType(i) == Types.FLOAT){
				sb.append(this.CreateHib("java.lang.Double", cname,rm.getColumnName(i)));
			}else{
				sb.append(this.CreateHib("java.lang.String", cname,rm.getColumnName(i)));
			}
		}
		sb.append("</class>\n\n");
		return sb.toString();
	}
	private String CreateHib(String type,String colName,String baseColName){
		StringBuffer sb =new StringBuffer();
		sb.append("    <property name=\""+colName+"\" type=\""+type+"\">\n");
		sb.append("        <column name=\""+baseColName.toLowerCase()+"\"/>\n");
		sb.append("    </property>\n");
		return sb.toString();
	}
	public static void main(String[] args) {
		try {
			System.out.println("==============start================");
			MySqlCreatePojoAndDao cp =new MySqlCreatePojoAndDao();
			System.out.println("===========创建pojo===================");
			cp.getTablePojo();
			System.out.println("===========创建dao====================");
			//cp.getTableDao();
			System.out.println("===========创建iservice====================");
			//cp.getTableIService();
			System.out.println("===========创建iservice实现类====================");
			//cp.getTableServiceImpl();
			System.out.println("===========创建Controller类====================");
			//cp.getTableController();
			System.out.println("===========生成Mapper文件====================");
			cp.getMapper();
			System.out.println("=================end======================");
			
			cp.openExplorer(cp.filePath);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/**
	 * 打开目录
	 * @param dir
	 */
	private void openExplorer(String dir){
		Runtime run = Runtime.getRuntime();   
	    try {   
	        // run.exec("cmd /k shutdown -s -t 3600");   
	        Process process = run.exec("cmd.exe /c start " + dir);   
	        InputStream in = process.getInputStream();     
	        while (in.read() != -1) {   
	            System.out.println(in.read());   
	        }   
	        in.close();   
	        process.waitFor();   
	    } catch (Exception e) {            
	        e.printStackTrace();   
	    }   

	}
}
