package cn.jerry.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

public class SensitiveWords {
	private boolean end = false;
	private HashMap<Character, SensitiveWords> words = null;

	private SensitiveWords getNextWords(char word) {
		return this.words == null ? null : this.words.get(word);
	}

	private void addNextWords(char currWord, SensitiveWords nextWords) {
		if (this.words == null) this.words = new HashMap<>();
		this.words.put(currWord, nextWords);
	}

	/**
	 * 添加一个敏感词
	 * 
	 * @param newWords
	 */
	public void addNewWords(String newWords) {
		if (newWords == null) return;
		newWords = newWords.trim();
		if (newWords.length() == 0) return;

		SensitiveWords parentWords = this, currWords = null;
		char[] charArr = newWords.toCharArray();
		for (int i = 0; i < charArr.length; i++) {
			currWords = parentWords.getNextWords(charArr[i]);
			if (currWords == null) {
				currWords = new SensitiveWords();
				parentWords.addNextWords(charArr[i], currWords);
			}
			// 循环终止
			if (i == charArr.length - 1) {
				currWords.end = true;
				currWords.words = null;
				return;
			}
			// 如果当前word已定义为结束，则表示本次添加的敏感词包含了现有的敏感词，忽略
			if (currWords.end) return;
			// 准备下一次循环
			parentWords = currWords;
		}
	}

	/**
	 * 删除一个敏感词
	 * 
	 * @param words
	 * @return
	 */
	public boolean removeWords(String words) {
		if (words == null) return false;
		words = words.trim();
		if (words.length() == 0) return false;

		SensitiveWords value = this.getNextWords(words.charAt(0));
		if (value != null) {
			if (value.end) {
				// 如果敏感词结束
				if (words.length() == 1) {
					// 如果提供的敏感词正好也是结束，说明完全匹配，删除
					this.words.remove(words.charAt(0));
					return this.words.isEmpty();
				} else {
					// 否则，说明不匹配，不删除
					return false;
				}
			} else {
				boolean childRemovedAll = value.removeWords(words.substring(1));
				if (childRemovedAll) {
					// 如果后续敏感字已全部删除，删除本字
					this.words.remove(words.charAt(0));
					return this.words.isEmpty();
				} else {
					return false;
				}
			}
		} else {
			return false;
		}
	}

	/**
	 * 查询一个字符串是否含有敏感词
	 * 
	 * @param string
	 * @return
	 */
	public Set<String> lookup(String string) {
		Set<String> sensitiveWords = new HashSet<>();
		if (string == null) return sensitiveWords;
		string = string.trim();
		if (string.length() == 0) return sensitiveWords;

		char[] charArr = string.toCharArray();
		StringBuilder temp = new StringBuilder();
		SensitiveWords start = this, value = null;
		for (int i = 0; i < charArr.length; i++) {
			value = start.getNextWords(charArr[i]);
			if (value != null) {
				temp.append(charArr[i]);
				if (value.end) {
					// 如果敏感词结束，查找完毕，添加到结果，清空缓存
					sensitiveWords.add(temp.toString());
					temp.setLength(0);
					start = this;
				} else {
					// 如果敏感词未结束，接着找
					start = value;
				}
			} else {
				if (temp.length() > 0) {
					// 敏感词不符合，回溯一个字，重新找
					temp.setLength(0);
					i--;
				}
				start = this;
			}
		}
		return sensitiveWords;
	}

	@Override
	public String toString() {
		return this.toStringSet().toString().replaceAll("\\[", "").replaceAll("\\]", "")
		        .replaceAll(", ", "\n");
	}

	private Set<String> toStringSet() {
		Set<String> values = new HashSet<>();
		if (this.words == null) {
			values.add("");
			return values;
		}

		for (Entry<Character, SensitiveWords> w : this.words.entrySet()) {
			Set<String> subValues = w.getValue().toStringSet();
			for (String subVal : subValues) {
				values.add(w.getKey() + subVal);
			}
		}
		return values;
	}
}
