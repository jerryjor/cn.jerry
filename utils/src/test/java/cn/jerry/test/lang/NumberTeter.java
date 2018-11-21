package cn.jerry.test.lang;

import java.util.List;

public class NumberTeter {
	public static void main(String[] args) {
		// // 中国格式
		// NumberFormat chinaFmt = NumberFormat.getInstance(Locale.CHINA);
		// chinaFmt.setGroupingUsed(true);
		// Number num = null;
		// // 基于格式的解析
		// try {
		// num = chinaFmt.parse("1,234.56rutyer11oituoC");
		// } catch (ParseException pe) {
		// System.err.println(pe);
		// }
		// System.out.println(num);
		// System.out.println(chinaFmt.format(num));
		// List<Integer> perHour = new ArrayList<Integer>();
		// perHour.add(1);
		// perHour.add(1);
		// perHour.add(1);
		// calcAccruedNum(perHour);
		// System.out.println(perHour);
		long st = System.currentTimeMillis();
		for (int i = 0; i < 1000000; i++) {
			splitNum2(100L + i % 100);
		}
		long et = System.currentTimeMillis();
		System.out.println("string method cost:" + (et - st) + "ms");
		st = System.currentTimeMillis();
		for (int i = 0; i < 1000000; i++) {
			splitNum1(100L + i % 100);
		}
		et = System.currentTimeMillis();
		System.out.println("math method cost:" + (et - st) + "ms");
	}

	private static void calcAccruedNum(List<Integer> perHour) {
		if (perHour == null) return;

		Integer preAccumulation = 0;
		for (int i = 0, size = perHour.size(); i < size; i++) {
			preAccumulation += perHour.get(i);
			perHour.set(i, preAccumulation);
		}
	}

	private static void calcAccruedAmount(List<Double> perHour) {
		if (perHour == null) return;

		Double preAccumulation = 0.00D;
		for (int i = 0, size = perHour.size(); i < size; i++) {
			preAccumulation += perHour.get(i);
			perHour.set(i, preAccumulation);
		}
	}

	private static Long[] splitNum1(Long num) {
		int length = new Double(Math.log10(num)).intValue() + 1;
		long rate = new Double(Math.pow(10, length)).longValue();
		Long[] nums = new Long[length];
		for (int i = 0; i < length; i++) {
			nums[i] = num / rate;
			num = num % rate;
			rate = rate / 10;
			// System.out.println(i + ":" + nums[i]);
		}
		return nums;
	}

	private static Long[] splitNum2(Long num) {
		String numStr = num == null ? "" : num.toString();
		int length = numStr.length();
		Long[] nums = new Long[length];
		for (int i = 0; i < length; i++) {
			nums[i] = Long.valueOf(numStr.substring(i, i + 1));
			// System.out.println(i + ":" + nums[i]);
		}
		return nums;
	}

}
