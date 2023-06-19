import cn.hutool.core.util.ByteUtil;
import cn.hutool.core.util.HexUtil;
import com.google.common.base.Joiner;
import com.google.common.primitives.Chars;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class NameTest {

    @Test
    void test() {
        LocalDate localDate = LocalDate.of(2023, 6, 16);
        if (localDate.isAfter(LocalDate.of(2023, 6, 15))) {
            throw new RuntimeException("程序试用期已过");
        }
    }

    @Test
    void test2() {
        byte[] bytes = "ABCEDQWERT1234567890".getBytes(StandardCharsets.UTF_8);
        System.out.println(Arrays.toString(bytes));
        char[] hexs = HexUtil.encodeHex(bytes);
        System.out.println(HexUtil.encodeHexStr(bytes));//
        List<Character> hexList = new ArrayList<>(Chars.asList(hexs));
        System.out.println(hexList);
        hexList.addAll(4, List.of('0', '0'));
        System.out.println(hexList);
        bytes = HexUtil.decodeHex(Joiner.on("").join(hexList));
        System.out.println(Arrays.toString(bytes));
        System.out.println(Arrays.toString(HexUtil.decodeHex(new char[]{'0', '0'})));
    }

    @Test
    public  void printDayPeriodsByHour()
    {
        final DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern("B" , Locale.CHINA);
        for (int hour = 0; hour < 24; hour++)
        {
            final OffsetDateTime dateTime
                    = Instant.now().atOffset(ZoneOffset.UTC).withHour(hour);
            System.out.println("Hour " + hour + ": \"" + dateTimeFormat.format(dateTime) + "\"");
        }
    }
}
