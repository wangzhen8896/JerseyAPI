package com.wzsuper.JerseyAPI.Utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 常见正则校验
 * @author wangz
 */
public class RegexUtils
{
    public enum Regex {
	/**
	 * 手机号
	 */
	PHONE("^(13[0-9]|15[012356789]|17[678]|18[0-9]|14[57])[0-9]{8}$"),
	/**
	 * 车牌号
	 * ^[京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领A-Z]{1}[A-Z]{1}[警京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼]{0,1}[A-Z0-9]{4}[A-Z0-9挂学警港澳]{1}$
	 */
	BUSNUM("(^[\u4E00-\u9FA5]{1,3}[A-Z0-9]{6}$)|(^[A-Z]{2}[A-Z0-9]{2}[A-Z0-9\u4E00-\u9FA5]{1}[A-Z0-9]{4}$)|(^[\u4E00-\u9FA5]{1}[A-Z0-9]{5}[挂学警军港澳临]{1}$)|(^[A-Z]{2}[0-9]{5}$)|(^(08|38){1}[A-Z0-9]{4}[A-Z0-9挂学警军港澳临]{1}$)");
	
	public String value;
	Regex(String regex){
	    this.value = regex;
	}
    }
    
    public static boolean isChinese(String value){
	return RegexCheck("^[\u4E00-\u9FA5]+$", value);
    }
    
    public static boolean RegexCheck(Regex regex, String value){
	if(NullOrEmptyUtil.isNotEmpty_all(regex, value)){
	    return RegexCheck(regex.value, value);
	}
	return false;
    }
    
    public static boolean RegexCheck(String regex, String value){
	if(NullOrEmptyUtil.isNotEmpty_all(regex, value)){
	    Pattern pattern = Pattern.compile(regex);
	    Matcher m = pattern.matcher(value);
	    return m.matches();
	}
	return false;
    }
}
