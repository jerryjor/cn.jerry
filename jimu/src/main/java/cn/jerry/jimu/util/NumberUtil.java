package cn.jerry.jimu.util;

import java.util.Random;

public class NumberUtil {
	private static Random r = new Random();
	private static int[] abnmSize = new int[100];
	static {
		for (int i = 0; i < 100; i++) {
			if (i < 5) {
				abnmSize[i] = 1;
			} else if (i < 10) {
				abnmSize[i] = 2;
			} else if (i < 15) {
				abnmSize[i] = 3;
				// } else if (i < 50) {
				// abnmSize[i] = 4;
			} else {
				abnmSize[i] = 5;
			}
		}
	}

	/**
	 * 返回变态方块的单元数
	 * 
	 * @return 单元数
	 */
	public static int getRandomSize() {
		int index = getRandomNum(100);
		return abnmSize[index];
	}

	/**
	 * 生成一个随机数字，范围[0，max)
	 * 
	 * @param max 不超过该值
	 * @param notEquals 不与该值重复
	 * @return 随机数字
	 */
	public static int getRandomNum(int max, int notEquals) {
		int num;
		do {
			num = getRandomNum(max);
		} while (num == notEquals);

		return num;
	}

	/**
	 * 生成一个随机数字，范围[0，max)
	 * 
	 * @param max 不超过该值
	 * @return 随机数字
	 */
	public static int getRandomNum(int max) {
		return r.nextInt(max);
	}

	/**
	 * 随机生成n个数字，可重复
	 * 
	 * @param count 个数
	 * @param maxNum 最大边界
	 * @return 数字数组
	 */
	public static int[] generateRandomNumbers(int count, int maxNum) {
		int[] nums = new int[count];
		for (int i = 0; i < count; i++) {
			int col = getRandomNum(maxNum);
			nums[i] = col;
		}
		return nums;
	}

	/**
	 * 判断某个int数组中是否存在某个数字
	 * 
	 * @param num 被检查的数字
	 * @param array 数字数组
	 * @return 数组中是否存在该数字
	 */
	public static boolean existsInArray(int num, int[] array) {
		boolean exists = false;
		for (int i = 0; i < array.length && !exists; i++) {
			exists = num == array[i];
		}
		return exists;
	}

	/**
	 * 格式化数字，数字超长时显示#
	 * 
	 * @param num 数字（整数）
	 * @param pattern 格式（9：不补；其他：补其他）
	 * @return 格式化后的字符串
	 */
	public static String formatInt(int num, String pattern) {
		char[] ca = pattern.toCharArray();
		String numString = String.valueOf(num);

		for (int i = 0, l = ca.length - numString.length(); i < l; i++) {
			numString = (ca[i] == '9' ? "" : String.valueOf(ca[i])) + numString;
		}

		boolean toLarge = numString.length() > pattern.length();
		return toLarge ? pattern.replaceAll(".", "#") : numString;
	}
}