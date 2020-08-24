package constant.read;

import com.alibaba.fastjson.JSON;
import constant.info.*;
import org.apache.commons.lang3.EnumUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

public class ExcelReaderUtil {
    public static void ReadData() throws IOException {
        String rootPath = "C:\\all_project\\sanqigame\\excel\\";
        List<GameExcelEnum> gameExcelEnumList = EnumUtils.getEnumList(GameExcelEnum.class);
        for (GameExcelEnum gameExcelEnum : gameExcelEnumList) {
            String fileName = gameExcelEnum.getName();
            File excelFile = new File(rootPath + fileName);
            if (!excelFile.exists()) {
                System.out.println("文件:" + excelFile.getAbsolutePath() + "不存在 跳过该文件");
                continue;
            }
            FileInputStream fileInputStream = new FileInputStream(excelFile);
            Workbook workbook = new HSSFWorkbook(fileInputStream);
            //获取sheet数量
            int sheets = workbook.getNumberOfSheets();

            //只读取第一个sheet
            for (int i = 0; i < sheets; i++) {
                //标题名即字段名

                Sheet sheet = workbook.getSheetAt(i);
                String sheetName = sheet.getSheetName();
                //该sheet对应的，类型
                Class<?> clazz = GameExcelSheetEnum.getClazzByName(sheetName);
                int rows = sheet.getPhysicalNumberOfRows();

                StringBuilder jsonData = new StringBuilder();
                jsonData.append("{");

                for (int excelRow = 0; excelRow < rows; excelRow++) {
                    Row row = sheet.getRow(excelRow);
                    String key  = row.getCell(0).getStringCellValue().trim();
                    int value = (int) row.getCell(1).getNumericCellValue();

                    jsonData.append("\"").append(key).append("\"").append(":").append(value).append(",");
                }
                String data = jsonData.substring(0, jsonData.length() - 1) + "}";
                System.out.println(data);

                Object parseObject = JSON.parseObject(data, clazz);
                try {
                    Method init = clazz.getDeclaredMethod("init", clazz);
                    init.setAccessible(true);
                    init.invoke(parseObject, parseObject);
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }

            }




        }
    }

    public static void main(String[] args) throws IOException {
        ExcelReaderUtil.ReadData();
        System.out.println(BackPackConst.getBackPackConst().toString());
        System.out.println(BossMonsterConst.getBossMonsterConst().toString());
        System.out.println(DuplicateConst.getDuplicateConst().toString());
        System.out.println(EquipmentConst.getEquipmentConst().toString());
        System.out.println(MailConst.getMailConst().toString());
        System.out.println(PotionConst.getPotionConst().toString());
        System.out.println(ProfessionConst.getProfessionConst().toString());
        System.out.println(TeamConst.getTeamConst().toString());
    }
}
