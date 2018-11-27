package com.mage.swagger02;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 *   定义Java类Salary {String name, int baseSalary, int bonus }，
 *   属性也随机产生（baseSalary范围是0-100，bonus为（0-5），其中name长度为4，随机字符串，
 *   然后进行排序，排序方式为收入总和（baseSalary*13 + bonus），输出收入最高的10个人的名单
 */
public class FileOperation {

    private static final int AMOUNT = 10000000;
    private static final String FILE_NAME = "test.txt";

    public static void main(String[] args) throws Exception {

        // 写入文件
        long start = System.currentTimeMillis();
        File file = writeBuffer();
//        writeNIO();
        System.out.println(String.format("写入时间：%d", System.currentTimeMillis() - start));

        // 读取文件
        start = System.currentTimeMillis();
         List<Salary> salaries =  readFileIO();
//        List<Salary> salaries = readFileNIO();
        System.out.println(String.format("解析文件所需时间：%d", System.currentTimeMillis() - start));
        start = System.currentTimeMillis();

        // 排序并截取前十
        sort(salaries);

        System.out.println(String.format("排序所需时间：%d", System.currentTimeMillis() - start));

    }

    /**
     * 写入文件
     * @return
     * @throws IOException
     */
    public static File writeBuffer() throws IOException {
        File file = new File(FILE_NAME);
        FileOutputStream fos = new FileOutputStream(file);
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fos));
        int i = AMOUNT;
        while(i > 0) {
            Salary salary = new Salary().build();
            writer.write(salary.toString() + "\r\n");
            i --;
        }
        writer.close();
        fos.close();
        return file;
    }

    /**
     * NIO进行写入
     * @throws IOException
     */
    private static void writeNIO() throws IOException {
        FileOutputStream fos = new FileOutputStream(FILE_NAME, true);
        FileChannel channel = fos.getChannel();
        int i = AMOUNT;
        StringBuffer content = new StringBuffer();
        while(i > 0) {
            Salary salary = new Salary().build();
            content.append(salary.toString()).append("\r\n");
            i --;
        }
        ByteBuffer buf = ByteBuffer.wrap(content.toString().getBytes());
        buf.put(content.toString().getBytes());
        buf.flip();
        channel.write(buf);
        channel.close();
        fos.close();

    }


    /**
     * Java IO读取文件的方式
     * @return
     * @throws Exception
     */
    public static List<Salary> readFileIO() throws Exception {
        File file = new File(FILE_NAME);
        List<Salary> list = new ArrayList<>();
        InputStreamReader reader = new InputStreamReader(new FileInputStream(file)); // 建立一个输入流对象reader
        BufferedReader br = new BufferedReader(reader); // 建立一个对象，它把文件内容转成计算机能读懂的语言
        String line = ""; // 每一行的内容
        int i = 1;
        while ((line = br.readLine()) != null) {
            String[] split = line.trim().split(" ");// .trim()可以去掉首尾多余的空格
            list.add(new Salary(split[0], Integer.valueOf(split[1]), Integer.valueOf(split[2]))); // 添加一个Salary实体
            i++;
        }
        reader.close();
        br.close();
        return list;
    }

    /**
     * JDK8 NIO读取文件
     * @return
     * @throws Exception
     */
    public static List<Salary> readFileNIO() throws Exception {
        List<Salary> list = new ArrayList<>();
        Files.lines(Paths.get(FILE_NAME)).forEach(line -> {
            String[] split = line.trim().split(" ");// .trim()可以去掉首尾多余的空格
            list.add(new Salary(split[0], Integer.valueOf(split[1]), Integer.valueOf(split[2]))); // 添加一个Salary实体
        });
        return list;
    }

    /**
     * 排序并获取前十数据
     * @param salaries
     */
    public static void sort(List<Salary> salaries) {
        Map<String, GroupSalary> result = new HashMap<>();
        salaries.forEach(salary -> {
            String shortName = salary.getName().substring(0, 2);
            GroupSalary groupSalary = null;
            List<Salary> salaryList = null;
            if (result.containsKey(shortName)) {
                groupSalary = result.get(shortName);
                salaryList = groupSalary.getSalaries();
            } else {
                groupSalary = new GroupSalary();
                salaryList = new ArrayList<>();
                groupSalary.setSalaries(salaryList);
            }
            salaryList.add(salary);
            groupSalary.setShortName(shortName);
            groupSalary.setTotal(groupSalary.getTotal() + salary.getBaseSalary() * 13 + salary.getBonus());
            result.put(shortName, groupSalary);
        });

        List<GroupSalary> r = result.entrySet().stream()
                .sorted((Map.Entry<String, GroupSalary> o1, Map.Entry<String, GroupSalary> o2) -> o2.getValue().getTotal() - o1.getValue().getTotal())
                .map(entry -> entry.getValue()).collect(Collectors.toList()).subList(0,10);

        r.forEach(groupSalary -> {
            System.out.println(groupSalary.getShortName() + " " + groupSalary.getTotal() + " " + groupSalary.getSalaries().size());
        });
    }

}

/**
 *  name 4位a-z随机
 *  baseSalary 0-100随机
 *  bonus 0-5随机
 *  年薪总额 = baseSalary * 13 + bonus
 */
class Salary {
    // name 4位a-z随机,baseSalary 0-100随机,bonus 0-5随机 年薪总额 = baseSalary * 13 + bonus
    private String name;
    private int baseSalary;
    private int bonus;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getBaseSalary() {
        return baseSalary;
    }

    public void setBaseSalary(int baseSalary) {
        this.baseSalary = baseSalary;
    }

    public int getBonus() {
        return bonus;
    }

    public void setBonus(int bonus) {
        this.bonus = bonus;
    }


    public Salary() {

    }

    public Salary(String name, int baseSalary, int bonus) {
        this.name = name;
        this.baseSalary = baseSalary;
        this.bonus = bonus;
    }

    public Salary build() {
        this.name = getRandomName(4);
        // 0-100随机数
        this.baseSalary = (int)(100 * Math.random());
        // 0-5随机数
        this.bonus = (int)(5 * Math.random());
        return this;
    }

    @Override
    public String toString() {
        return name + " " + baseSalary + " " + bonus;
    }

    /**
     * 生产Name随机函数 4位a-z随机
     * @param length
     * @return
     */
    private static String getRandomName(int length ){
        String base = "abcdefghijklmnopqrstuvwxyz";
        Random random = new Random();
        StringBuffer sb = new StringBuffer();
        for ( int i = 0; i < length; i++ ){
            int number = random.nextInt(base.length());
            sb.append(base.charAt(number));
        }
        return sb.toString();
    }
}

class GroupSalary {
    private List<Salary> salaries;
    private String shortName;
    private int total;

    public List<Salary> getSalaries() {
        return salaries;
    }

    public void setSalaries(List<Salary> salaries) {
        this.salaries = salaries;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }


}

