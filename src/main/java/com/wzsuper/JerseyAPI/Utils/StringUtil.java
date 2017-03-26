package com.wzsuper.JerseyAPI.Utils;

import org.apache.commons.lang.StringUtils;

public class StringUtil extends StringUtils{
	
	public boolean isNullOrEmpty(Object obj){
		if(obj == null || isEmpty(obj.toString()) || isEmpty(obj.toString().trim())){
			return true;
		}
		return false;
	}
	
	public boolean isNullOrEmpty(Object... objs){
		 if ( objs != null && objs.length > 0 )
	        {
	            int objsLen = objs.length;
	            for (int i = 0; i < objsLen; i++)
	            {
	                if (isNullOrEmpty(objs[i]))
	                {
	                    return false;
	                }
	            }
	        }
		 return true;
	}
}
