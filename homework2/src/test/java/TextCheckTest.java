import com.dai.TextCheck;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class TextCheckTest {

    public static List<String> waitCheckList = new ArrayList<String>();

    static {
        waitCheckList.add("E:\\RuanGong\\DhxWork\\homework2\\src\\test\\resources\\orig_0.8_add.txt");
        waitCheckList.add("E:\\RuanGong\\DhxWork\\homework2\\src\\test\\resources\\orig_0.8_del.txt");
        waitCheckList.add("E:\\RuanGong\\DhxWork\\homework2\\src\\test\\resources\\orig_0.8_dis_1.txt");
        waitCheckList.add("E:\\RuanGong\\DhxWork\\homework2\\src\\test\\resources\\orig_0.8_dis_10.txt");
        waitCheckList.add("E:\\RuanGong\\DhxWork\\homework2\\src\\test\\resources\\orig_0.8_dis_15.txt");
    }

    @Test
    public  void test() {
        TextCheck.main(new String[]{
                // 原文路径
                "E:\\RuanGong\\DhxWork\\homework2\\src\\test\\resources\\orig.txt",
                // 待校验文件路径
                waitCheckList.get(4),

                // 结果输出路径
                "E:\\RuanGong\\DhxWork\\homework2\\src\\test\\resources\\result.txt"}
        );
    }
}
