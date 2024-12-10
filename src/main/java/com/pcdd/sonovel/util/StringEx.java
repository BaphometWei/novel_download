
package com.pcdd.sonovel.util;

import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



// Referenced classes of package ctais.util:
//            DateEx

/**
 * 字符串操作工具类
 * @author foresee
 * 2002-08-08
 */
public class StringEx {


    public StringEx() {
    }

    public static final int indexOf(StringBuffer source, int start, String tobelocated) {
        return source.toString().indexOf(tobelocated, start);
    }

    public static String replace(String str, String oldStr, String newStr) {
        if (null == str || null == oldStr || newStr == null)
            return str;
        if (str.length() > 0xf4240)
            try {
                throw new Exception("警告：替换的字符串超过1M,可能造成JVM抖动!");
            } catch (Exception ex) {

            }
        int len = oldStr.length();
        String post = str;
        StringBuffer resStr = new StringBuffer("");
        int idx;
        while ((idx = post.indexOf(oldStr)) != -1) {
            String pre = post.substring(0, idx);
            post = post.substring(idx + len);
            resStr.append(pre).append(newStr);
        }
        resStr.append(post);
        return resStr.toString();
    }

    public static String[] splitString(String str, String splitter) {
        if (str == null)
            return null;
        if (null == splitter || splitter.length() == 0)
            return (new String[]{
                    str
            });
        int p = str.indexOf(splitter);
        int prev = 0;
        if (p < 0) {
            String ss[] = new String[1];
            ss[0] = str;
            return ss;
        }
        ArrayList found = new ArrayList();
        String s = null;
        for (; p >= 0; p = str.indexOf(splitter, prev)) {
            if (prev == p)
                s = "";
            else
                s = str.substring(prev, p);
            found.add(s);
            prev = p + 1;
        }

        if (prev < str.length())
            found.add(str.substring(prev, str.length()));
        return (String[]) found.toArray(new String[1]);
    }

    public int occurence(String str, String s) {
        if (null == str || null == s)
            return 0;
        int p = str.indexOf(s);
        int cnt = 0;
        if (p < 0)
            return cnt;
        int len = s.length();
        for (cnt++; p < str.length(); cnt++) {
            p += len;
            p = str.indexOf(s, p);
            if (p < 0)
                return cnt;
        }

        return cnt;
    }


    public static String sNull(Object obj) {
        if (obj == null) {
            return "";
        }
        if (obj != null && obj instanceof BigDecimal) {
            return ((BigDecimal) obj).toPlainString();
        }
        else if (obj != null && obj instanceof String) {
            if ("null".equalsIgnoreCase((String) obj) || "undefined".equalsIgnoreCase((String) obj)) {
                return "";
            } else {
                return obj.toString();
            }
        }
        return obj.toString();
    }

    /**
     * 返回字符串
     * @param bigDecimal
     * @return 如果为空，返回0
     */
    public static String sNull(BigDecimal bigDecimal) {
        return bigDecimal != null ? bigDecimal.toPlainString() : "0";
    }


    public static String sNull(String obj) {
    	 if(obj!=null){
    		 if(obj.toLowerCase().equals("null")||obj.toLowerCase().equals("undefined")){
    			 return "";
    		 }else{
    			 return obj;
    		 }
    	 }else{
    		 return "";
    	 }
    }
     
     /**
      * 包括null判断
     * @param obj
     * @return
     */
    public static String sNull2(String obj) {
    	 if(obj!=null){
    		 if(obj.toLowerCase().equals("null")||obj.toLowerCase().equals("undefined")){
    			 return "";
    		 }else{
    			 return obj;
    		 }
    	 }else{
    		 return "";
    	 }
     }
     
     
     /**
      * 返回字符串
     * @param obj
     * @return 如果为空，返回0
     */
    public static String dNull(Object obj) {
         return obj != null ? obj.toString() : "0";
     }
     
     /**
      * 返回字符串
     * @param obj
     * @return 如果为空，返回0
     */
    public static String dNull(String obj) {
         return obj != null ? obj.toString() : "0";
     }
     
    /**
     *
     * @param dblval
     * @return
     * @history 2006-09-23
     */
    public static String sNull(double dblval){
        String value = String.valueOf(dblval);
        return  sNull(value) ;
    }
    
    public static String sNull(Object obj, boolean isconvert) {
        if (isconvert)
            return obj != null ? obj.toString() : "";
        else
            return obj != null ? obj.toString() : null;
    }

    public static String convGbk(String arg){
        if (arg == null) {
            return arg;
        } else {
            String s = null;
            byte b[];
			try {
				b = arg.getBytes("ISO-8859-1");
				 s = new String(b, "GBK");
			} catch (UnsupportedEncodingException e) {

			}
           
            return s;
        }
    }

    public static String reverseGbk(String arg)
            throws Exception {
        if (arg == null) {
            return null;
        } else {
            String s = null;
            byte b[] = arg.getBytes("GBK");
            s = new String(b, "ISO-8859-1");
            return s;
        }
    }

    public static String convertCharSet(String arg, String source, String target)
            throws Exception {
        if (null == arg || null == source || null == target) {
            return arg;
        } else {
            String s = null;
            byte b[] = arg.getBytes(source);
            s = new String(b, target);
            return s;
        }
    }

    public static String replMark(String s) {
        if (null == s)
            return null;
        else
            return s.replace('<', '[').replace('>', ']');
    }

    public static String getParm(Hashtable inputParms, String key) {
        if (null == key || null == inputParms)
            return null;
        Object o1 = inputParms.get(key);
        if (o1 instanceof String)
            return (String) inputParms.get(key);
        if (o1 instanceof String[]) {
            String s1[] = (String[]) o1;
            if (s1 != null)
                return s1[0];
        }
        return null;
    }

    /**
     * 判断是否有特殊字符，如有，返回字段不为空
     * @param str
     * @return
     */
    public static String hasSpecialWord(String str) {
    	StringBuffer stringBuffer = new StringBuffer();
    	String regEx = "[$%]"; //特殊字符正则表达式  
		Pattern p = Pattern.compile(regEx);  
		Matcher m = p.matcher(str);  
		while (m.find()) {   
			stringBuffer.append("“"+m.group(0)+"”,");  
		}
		String result = stringBuffer.toString();
		if(result.length()>0){
			result = result.substring(0,result.length()-1);
		}
        return result;
    }


    /**
     * 是否为空判断
     * 包括null判断
     * @param str
     * @return 如果为空，返回true
     */
    public static boolean isEmpty(String str) {
        boolean empty = false;
        if (str == null || str.equals("") || str.toLowerCase().equals("null"))
            empty = true;
        return empty;
    }

    /**
     *
     * @param str
     * @return
     * @history add by lf 2006-10-08
     */
     public static boolean isNotEmpty(String str) {
         return !isEmpty(str);

    }
     
//     /**
//      * 
//      * @param old
//      * @param nulldef
//      * @return
//      */
//     public static String isNull(Object old, String nulldef) {
//    	 return isNull(anyToString(old),nulldef);
//     }
     
    /**
     * @param old
     * @param nulldef
     * @return if 为空 then return def else old.
     */
    public static String isNull(String old, String nulldef) {
        if (isEmpty(old)) {
            return nulldef;
        }
        return old;
    }

    /**
     * @param str
     * @return 返回首个字符大写的字符串
     */
    static public String capitalise(String str) {
        if (str == null) {
            return null;
        }
        if (str.length() == 0) {
            return "";
        }
        return new StringBuffer(str.length()).append(Character.toTitleCase(str.charAt(0))).append(str.substring(1)).toString();
    }

    /**
     *
     * @param e
     * @return  stackTrace
     */
    static public String stackTrace(Throwable e) {
        java.io.ByteArrayOutputStream buf;
//        Exception f;
        String foo;
        foo = null;
        try {
            buf = new java.io.ByteArrayOutputStream();
            PrintWriter pw = new java.io.PrintWriter(buf, true);
            e.printStackTrace(pw);
            foo = buf.toString();
        }
        catch (Exception e4) {

        }
        return foo;
    }

    public static String RuleHTMLStr(String srcStr)
    {
        if(srcStr == null || srcStr.trim().length() <= 0)
            srcStr = "";
        return toHTML(srcStr);
    }

    private static String toHTML(String srcStr)
    {
        if(srcStr == null || srcStr.length() <= 0)
            return "";
        String TempStr = "";
        for(int i = 0; i < srcStr.length(); i++)
        {
            int TempInt = srcStr.charAt(i);
            if(32 <= TempInt && TempInt <= 47)
                TempStr = TempStr + "&#" + Integer.toString(TempInt) + ";";
            else
            if(58 <= TempInt && TempInt <= 64)
                TempStr = TempStr + "&#" + Integer.toString(TempInt) + ";";
            else
            if(91 <= TempInt && TempInt <= 96)
                TempStr = TempStr + "&#" + Integer.toString(TempInt) + ";";
            else
            if(123 <= TempInt && TempInt <= 126)
                TempStr = TempStr + "&#" + Integer.toString(TempInt) + ";";
            else
                TempStr = TempStr + srcStr.charAt(i);
        }

        return TempStr;
    }

     /**
     * 将byte数组转换为表示16进制值的字符串，
     * 如：byte[]{8,18}转换为：0813，
     * 和public static byte[] hexStr2ByteArr(String strIn)
     * 互为可逆的转换过程
     *
     * @param arrB 需要转换的byte数组
     * @return 转换后的字符串
     * @throws Exception 本方法不处理任何异常，所有异常全部抛出
     */
    public static String byteArr2HexStr(byte[] arrB)
            throws Exception {
        int iLen = arrB.length;
        //每个byte用两个字符才能表示，所以字符串的长度是数组长度的两倍
        StringBuffer sb = new StringBuffer(iLen * 2);
        for (int i = 0; i < iLen; i++) {
            int intTmp = arrB[i];
            //把负数转换为正数
            while (intTmp < 0) {
                intTmp = intTmp + 256;
            }
            //小于0F的数需要在前面补0
            if (intTmp < 16) {
                sb.append("0");
            }
            sb.append(Integer.toString(intTmp, 16));
        }
        return sb.toString();
    }


    /**
     * 将表示16进制值的字符串转换为byte数组，
     * 和public static String byteArr2HexStr(byte[] arrB)
     * 互为可逆的转换过程
     *
     * @param strIn 需要转换的字符串
     * @return 转换后的byte数组
     * @throws Exception 本方法不处理任何异常，所有异常全部抛出

     */
    public static byte[] hexStr2ByteArr(String strIn)
            throws Exception {
        byte[] arrB = strIn.getBytes();
        int iLen = arrB.length;

        //两个字符表示一个字节，所以字节数组长度是字符串长度除以2
        byte[] arrOut = new byte[iLen / 2];
        for (int i = 0; i < iLen; i = i + 2) {
            String strTmp = new String(arrB, i, 2);
            arrOut[i / 2] = (byte) Integer.parseInt(strTmp, 16);
        }
        return arrOut;
    }

    /**
     *
     * @param querystring    url参数
     * @return   执分后的参数Map
     */
    public static HashMap getParameterMap(String querystring) {
        HashMap hashMap = new HashMap();
        String parameterString = querystring;

        //从?后开始拆分参数
        int pos = querystring.indexOf("?");
        if (pos > 0) {
            parameterString = querystring.substring(pos + 1);
        }
        //分解参数列表
        String[] allParameters = parameterString.split("&");
        if (allParameters != null && allParameters.length > 0) {
            for (int i = 0; i < allParameters.length; i++) {
                String parameterNameValue = allParameters[i];
                //分解每个参数的值
                String[] nameValue = parameterNameValue.split("=");
                //给参数实体类付值
                if (nameValue.length == 2) {         //并判断参数是否有值
                    hashMap.put(nameValue[0], nameValue[1]);
                }
            }
        }
        return hashMap;
    }
    
    /**
     * 格式化字符串，保留两位小数 ####0.00
     * @param str
     * @return 
     */
    public static String formatDecimal(String str){
    	//返回结果
    	String result = "";
    	if(!StringEx.isEmpty(str)){
    		DecimalFormat decimalFormat = new DecimalFormat("####0.00"); 
			double temp = Double.parseDouble(str);
			result = decimalFormat.format(temp);
    	}
    	return result;
    }
    
    /**
     * 格式化字符串，保留两位小数 ####0.00
     * @param number
     * @return 
     */
    public static String formatDecimal(double number){
    	//返回结果
    	String result = "";
		DecimalFormat decimalFormat = new DecimalFormat("####0.00");
		result = decimalFormat.format(number);
    	return result;
    }
    
    /**
     * 格式化字符串，不保留小数
     * @param str
     * @return 
     */
    public static String formatDecimal0(String str){
    	//返回结果
    	String result = "";
    	if(!StringEx.isEmpty(str)){
    		DecimalFormat decimalFormat = new DecimalFormat("0"); 
			double temp = Double.parseDouble(str);
			result = decimalFormat.format(temp);
    	}
    	return result;
    }
    
    /**
     * 格式化字符串，如果没有小数或者小数点后小于两位，保留两位小数 ####0.00
     * @param str
     * @return
     */
    public static String formatDecimal2(String str){
    	//返回结果
    	String result = "";
    	if(!StringEx.isEmpty(str)){
    		int dotIndex = str.lastIndexOf(".");
    		if(dotIndex==-1||(str.length()-dotIndex)<=3){
    			result = StringEx.formatDecimal(str);
    		}else{
    			//类似.4854369数据，前面补零，变为0.4854369
    			if(dotIndex==0){
    				str = "0"+str;
    			}
    			result = str;
    		}
    	}
    	return result;
    }
    
    /**
     * 字符串转换成Double
     * @param str
     * @return
     */
//    public static double sToDouble(String str){
//    	//返回结果
//    	if (!"".equals(sNull(str)))
//			return new Double(sNull(str)).doubleValue();
//		else
//			return 0.00;
//    }
    
    
    /**
     * 半角转换成全角
     * @param bjStr 半角字符
     * @return 全角字符
     */
    public static String BJ2QJ(String bjStr) {  //去掉半角转换全角的操作，发现在linux下面会把(给去掉  modify by chenquan at 2013.01.24
    	if(bjStr == null){
    	   return "";
    	}else{
    		return bjStr;
    	}
    	
    	/*
    	StringBuffer DBCString = new StringBuffer();
    	try {
    	   byte[] bytes = null;
    	   for (int i = 0; i < bjStr.length (); i++) {
		    	String temp = bjStr.substring(i, i+1);
		  //  	boolean isContainLetters = Pattern.matches("[`~!@#$%^&*\\(\\)+=|{}':;',\\[\\].<>/?~%……&*——+|‘”“’]" , temp);
		    	boolean isContainLetters = Pattern.matches("[`~!@#$%^&*+=|{}':;',\\[\\].<>/?~%……&*——+|‘”“’]" , temp);
		    	if (isContainLetters) {//对相应半角特殊字符转换成全角
		    	   bytes = temp.getBytes ("unicode");
		    	   if (bytes[3] != -1){
				    	bytes[2] = (byte)(bytes[2]-32);
				    	bytes[3] = -1;
		    	   }
		    	   DBCString.append (new String (bytes,"unicode"));
		    	}else{
		    	   DBCString.append (temp);
		    	}
    	   }
    	} catch (Exception e) {
    	   e.printStackTrace ();
    	}
    	return DBCString.toString().trim();
    	*/
   }
   
	/**
	 * gbk转换和半角转全角
	 * @param obj
	 * @return
	 */
//	public static String toGBK(Object obj) {
//		return StringEx.BJ2QJ(DataChasetUtil.toGBK(StringEx.sNull(obj)));
//	}
	
	/**
	 * replaceAll(String regex, String replacement)中的replacement参数即替换内容中含有特殊字符$时，需转义
	 * @param str
	 * @return
	 */
	public static String replaceAllBefore(String str) {
		if(StringEx.isEmpty(str)){
			return "";
		}else{
			str = str.replaceAll("\\$", "\\\\\\$");  
			return str;
		}
	}
	
	/**
	 * xml特殊字符转换
	 * &转成&amp;
	 * <转成&lt;
	 * >转成&gt;
	 * @param str
	 * @return
	 */
	public static String replaceAllBeforeXml(String str) {
		if(StringEx.isEmpty(str)){
			return "";
		}else{
			str = str.replaceAll("&", "&amp;");  
			str = str.replaceAll("<", "&lt;");  
			str = str.replaceAll(">", "&gt;");  
//			str = str.replaceAll("'", "&apos;");  
//			str = str.replaceAll("\"", "&quot;");  
			return str;
		}
	}
	
	/**
	 * gbk转换和半角转全角
	 * @param obj
	 * @return
	 */
//	public static String toGBK(String obj) {
//		return StringEx.BJ2QJ(DataChasetUtil.toGBK(StringEx.sNull(obj)));
//	}
//
//	 public static String anyToString(Object obj) {
//	        String value = "";
//	        if (obj != null)
//	            if (obj instanceof Timestamp)
//	                value = DateTimeUtils.formatDate((Date) obj, "yyyy-MM-dd");
//	            else
//	                value = obj.toString();
//	        return value;
//	    }
	 
	 /**
	     * Substitutes <code>searchString</code> in the given source String with <code>replaceString</code>.<p>
	     * 
	     * This is a high-performance implementation which should be used as a replacement for 
	     * <code>{@link String#replaceAll(java.lang.String, java.lang.String)}</code> in case no
	     * regular expression evaluation is required.<p>
	     * 
	     * @param source the content which is scanned
	     * @param searchString the String which is searched in content
	     * @param replaceString the String which replaces <code>searchString</code>
	     * 
	     * @return the substituted String
	     */
	    public static String substitute(String source, String searchString, String replaceString) {

	        if (source == null) {
	            return null;
	        }

	        if (isEmpty(searchString)) {
	            return source;
	        }

	        if (replaceString == null) {
	            replaceString = "";
	        }
	        int len = source.length();
	        int sl = searchString.length();
	        int rl = replaceString.length();
	        int length;
	        if (sl == rl) {
	            length = len;
	        } else {
	            int c = 0;
	            int s = 0;
	            int e;
	            while ((e = source.indexOf(searchString, s)) != -1) {
	                c++;
	                s = e + sl;
	            }
	            if (c == 0) {
	                return source;
	            }
	            length = len - (c * (sl - rl));
	        }

	        int s = 0;
	        int e = source.indexOf(searchString, s);
	        if (e == -1) {
	            return source;
	        }
	        StringBuffer sb = new StringBuffer(length);
	        while (e != -1) {
	            sb.append(source.substring(s, e));
	            sb.append(replaceString);
	            s = e + sl;
	            e = source.indexOf(searchString, s);
	        }
	        e = len;
	        sb.append(source.substring(s, e));
	        return sb.toString();
	    }
}
