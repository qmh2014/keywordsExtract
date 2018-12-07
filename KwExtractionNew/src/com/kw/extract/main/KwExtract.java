package com.kw.extract.main;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSONObject;

public class KwExtract {
	private Logger logger = Logger.getLogger(KwExtract.class);
	
	public JSONObject extractKws(String kws) {
		JSONObject json = new JSONObject();
		try {		
			
			MyStack<Character> myStack_logic = new MyStack<Character>();//记录()|&的栈
			MyStack<String> keywords_no_logic = new MyStack<String>();//记录关键词的栈
			String pre = "";
			for(int i = 0; i < kws.length(); i++) {
				char c = kws.charAt(i);
				if(c == '(' || c == ')' || c == '|' || c == '&') {
					if(pre.length() > 0) {
						keywords_no_logic.push(pre);
						pre = "";
					}
					if(c == '(' || c == '|' || c == '&') {
						myStack_logic.push(c);
					} else if(c == ')') {
						//需要弹出最近的一个左括号
						if(myStack_logic.size() == 0) {
							//解析失败
							json.put("status", "-1");
							json.put("msg", "格式不准确，检查括号是否匹配");
							return json;
						}
						List<String> cur_keywords_content = new ArrayList<String>();
						Set<Character> set = new HashSet<Character>();
						char pop_char = '0';
						String pop_str = "";
						char join_character = '0';
						while(myStack_logic.size() > 0 && (pop_char = myStack_logic.pop()) != '(' 
								&& keywords_no_logic.size() > 0 && (pop_str = keywords_no_logic.pop()) != null) {
							set.add(pop_char);
							cur_keywords_content.add(pop_str);
							join_character = pop_char;
						}
						pop_str = keywords_no_logic.pop();
						cur_keywords_content.add(pop_str);//弹出最后一个词
						
						if(set.size() > 1) {
							//关键词规则不准确
							json.put("status", "-1");
							json.put("msg", "格式不准确，检查不同&|关系是否用括号隔开");
							return json;
						} else if(set.size() == 0) {
							//说明括号中没有&|
							//cur_and = pop_str;
							keywords_no_logic.push(pop_str);
						} else if(set.size() == 1){
							if(join_character != '&' && join_character != '|') {
								json.put("status", "-1");
								json.put("msg", "格式不准确，检查是否有除|&之外的字符存在");
								return json;
							}
							String combineDatas = computKwsByAndOr(cur_keywords_content, join_character);
							keywords_no_logic.push(combineDatas);
							cur_keywords_content.clear();
							
						}
						
						
					}
				} else {
					//当前字符不是特殊字符
					pre += c;
				}
			}
			if(keywords_no_logic.size() == 1 && myStack_logic.size() == 0) {
				//计算结束
				
				json.put("status", 0);
				json.put("data", reverse(keywords_no_logic.pop()));
				return json;
			}
			//如果myStack_logic还有元素且keywords_no_logic也不为空，需要继续运算。
			char pop_char = '0';
			String pop_str = "";
			char join_character = '0';
			Set<Character> set = new HashSet<Character>();
			List<String> cur_keywords_content = new ArrayList<String>();
			while(myStack_logic.size() > 0 && (pop_char = myStack_logic.pop()) != '0'
					&& keywords_no_logic.size() > 0 && (pop_str = keywords_no_logic.pop()) != null) {
				set.add(pop_char);
				cur_keywords_content.add(pop_str);
				join_character = pop_char;
			}
			if(keywords_no_logic.size() == 0) {
				json.put("status", -1);
				json.put("msg", "检查格式是否正确，左右括号不对应");
				return json;
			}
			pop_str = keywords_no_logic.pop();
			cur_keywords_content.add(pop_str);//弹出最后一个词
			if(set.size() == 1 && (join_character == '|' || join_character == '&')) {
				String res = computKwsByAndOr(cur_keywords_content, join_character);
				json.put("status", 0);
				json.put("data", reverse(res));
				return json;
			} else {
				json.put("status", -1);
				json.put("data", "检查是否有不同&|的没加括号！或者出现除()|&的其他符号存在");
				return json;
			}
		} catch(Exception e) {
			e.printStackTrace();
			json.put("status", -1);
			json.put("msg", "检查格式是否正常");
			return json;
		}
		
	}
	
	public String computKwsByAndOr(List<String> cur_keywords_content, char join_character) {
		String result_str = null;
		if(join_character == '&') {
			//空格分割并的关系
			//其中情况需要多判断几句
			String cur_res = null;
			StringBuffer temp = new StringBuffer();
			for(String s : cur_keywords_content) {
				if(cur_res == null) {
					cur_res = s;
				} else {
					//对每个或的都加下当前元素
					String[] or_arr = cur_res.split("#");
					for(String inner : or_arr) {
						String[] or_arr_inner = s.split("#");
						for(String inner_inner: or_arr_inner) {
							temp.append(inner + " " + inner_inner + "#");
						}
					}
					if(temp.length() > 0) {
						cur_res = temp.substring(0, temp.length() - 1);//去掉最后一#			
					}
					temp = new StringBuffer();
				}
			}						
			result_str = cur_res;
		} else if(join_character == '|') {
			//#分割或的关系
			//此种情况较为简单，直接追加
			result_str = String.join("#", cur_keywords_content);
			
		}
		return result_str;
	}
	//因为栈是从后往前的顺序，因此最终结果需要调整一下顺序
	public String reverse(String data) {
		String[] res_arr = data.split("#");
		List<String> list = new ArrayList<String>();
		for(String str : res_arr) {
			String[] arr = str.split(" ");
			if(arr.length > 1) {
				StringBuffer one_reverse = new StringBuffer();
				for(int i = arr.length - 1; i >= 0; i--) {
					one_reverse.append(arr[i] + " ");
				}
				list.add(one_reverse.substring(0, one_reverse.length() - 1));
			} else {
				list.add(str);
			}
			
		}
		Collections.reverse(list);
		return String.join("#", list);
	}
	public static void main(String[] args) {
		//PropertyConfigurator.configure("config" + File.separator + "log4j.properties");
		//String kws = "((中共|八路军|共军|共产党|十八集团军)&(游而不击|抗战动机不纯|抢地盘|通敌卖国|(七分发展&一分抗日)))|((国民党|国军)&(抗日|抗战)&主力)|(中国&(抗战|抗日)&真相)";
		//String kws = "(G20&中国&(互联网|人工智能|AI)&(腾讯|阿里|阿里巴巴|百度|京东))";
		String kws = "(G20)&(中国)&(人工智能|AI)&(腾讯|阿里|百度|京东)";
		KwExtract extract = new KwExtract();
		JSONObject json = extract.extractKws(kws);
		System.out.println(json.toJSONString());
	}

}
