package client.scene;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.EnumUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * @author 张丰博
 */
@Slf4j
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
                List<String> excelTitleNames = new ArrayList<>();
                List<String> excelData = new ArrayList<>();

                Sheet sheet = workbook.getSheetAt(i);

                String sheetName = sheet.getSheetName();
                //该sheet对应的，类型
                Class<?> clazz = GameExcelSheetEnum.getClazzByName(sheetName);


                int rows = sheet.getPhysicalNumberOfRows();
                Row titleRow = sheet.getRow(0);
                for (int titleColNum = 0; titleColNum < titleRow.getPhysicalNumberOfCells(); titleColNum++) {
                    excelTitleNames.add(titleRow.getCell(titleColNum).getStringCellValue().trim());
                }
                for (int excelRow = 1; excelRow < rows; excelRow++) {
                    StringBuilder jsonData = new StringBuilder();
                    jsonData.append("{");
                    Row row = sheet.getRow(excelRow);
                    int colNums = row.getPhysicalNumberOfCells();

                    for (int colNum = 0; colNum < colNums; colNum++) {
                        Cell cell = row.getCell(colNum);
                        CellType excelColType = cell.getCellType();
                        Object columnValue = "";
                        if (excelColType == CellType.STRING) {
                            columnValue = row.getCell(colNum).getStringCellValue().trim();
                        } else if (excelColType == CellType.NUMERIC) {
                            columnValue = row.getCell(colNum).getNumericCellValue();
                        }
                        if (excelTitleNames.get(colNum).equals("rewardProps")){
                            jsonData.append(excelTitleNames.get(colNum) + ":" + columnValue.toString() + ",");
                        }else {
                            jsonData.append(excelTitleNames.get(colNum) + ":\"" + columnValue.toString() + "\",");
                        }
                    }
                    excelData.add(jsonData.substring(0, jsonData.length() - 1) + "}");
                }

//                System.out.println(excelData);

                // 对数据进行处理
                List<?> result = new ArrayList<>();
                for (String jsonData : excelData) {
                    result.add(JSON.parseObject(jsonData, (Type) clazz));
                }

                GameData gameData = GameData.getInstance();
                String methodName = "set" + clazz.getSimpleName() + "List";
                Method[] methods = GameData.class.getMethods();
                for (Method method : methods) {
                    if (!methodName.equals(method.getName())) {
                        continue;
                    }
                    try {
                        method.invoke(gameData, result);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        log.error(e.getMessage(), e);
                    }
                }


            }

        }
    }

    public static void main(String[] args) throws IOException {
        ExcelReaderUtil.ReadData();
        GameData gameData = GameData.getInstance();
        gameData.initGameData();
    }
}
