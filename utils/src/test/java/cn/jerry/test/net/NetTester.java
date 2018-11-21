package cn.jerry.test.net;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class NetTester {
	// http://old.sxmb.gov.cn/vip/2015qh.aspx
	// __VIEWSTATE=%2FwEPDwUKMTk0ODU1MTk1Mg9kFgICAw9kFgICAQ8PFgIeB0VuYWJsZWRnZGQYAQUeX19Db250cm9sc1JlcXVpcmVQb3N0QmFja0tleV9fFhQFCeaItOWWnOe6ogUG5aes5Y2HBQbmnZzmlocFCemrmOW%2Fl%2BaWjAUJ6auY56S%2B5YW1BQnmnajmloflubMFBuWImOeHlQUG5byg6ZOtBQnotbXmmZPls7AFBueoi%2BWHrwUJ6Zu35pmT6IuxBQbliJjnuqIFCeeOi%2BawuOiMggUG5p2O5LicBQnnjovmtanlvLoFBueoi%2BazogUG5byg5LquBQnmrablhbTljpoFCeW%2BkOS4luaciQUJ5p2O54yu5YabHiuZJ%2FmAKPmVtNuxAKpQ7BpGqVb%2FI4nie8ffQ8PO258%3D
	// &__EVENTVALIDATION=%2FwEWGAKvnMirDAKMz4qLBALE3I%2F4DwL0qY%2FCDgL20tKLDgLZ%2BJGaCQLz8rPqCwK409fkDQKA2bfLAwKlirbeBwKlo6%2FjDwK%2B1KLsCAK406OZDgLF2O9HAuKp%2B54BAp2qpvsFAqWjo4sPAoDZs5kBAtKns0AC3YXywAYCleXN2g0CjOeKxgYC7NGy6wYC7NH22Qw2Jw3Ph2xgslhrzdpY6LVQV9M6c11EoKtGVvwHyBS2Sg%3D%3D
	// 张亮
	// &%E5%BC%A0%E4%BA%AE=on
	// 提交投票
	// &Button1=%E6%8F%90%E4%BA%A4%E6%8A%95%E7%A5%A8
	// &TextBox1=
	// &TextBox2=

	public static void main(String[] args) {
		try {
			InetAddress.getByName("www.baidu.com");
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
