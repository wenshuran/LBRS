package com.gatech.bigdata.hbase;

import com.gatech.bigdata.mybatisplus.service.UserVectorService;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;

@Service
public class HBaseService {
    private Logger log = LoggerFactory.getLogger(HBaseService.class);

    @Autowired
    private UserVectorService userVectorService;

    private Admin admin = null;
    private Connection connection = null;
    public HBaseService() {
        try {
            connection = ConnectionFactory.createConnection(HBaseConfiguration.create());
            admin = connection.getAdmin();
        } catch (IOException e) {
            log.error("Error connecting Hbase!");
        }
    }

    public boolean creatTable(String tableName, List<String> columnFamily) {
        try {
            List<ColumnFamilyDescriptor> cfDesc = new ArrayList<>(columnFamily.size());
            columnFamily.forEach(cf -> {
                cfDesc.add(ColumnFamilyDescriptorBuilder.newBuilder(
                        Bytes.toBytes(cf)).build());
            });
            TableDescriptor tableDesc = TableDescriptorBuilder
                    .newBuilder(TableName.valueOf(tableName))
                    .setColumnFamilies(cfDesc).build();
            if (admin.tableExists(TableName.valueOf(tableName))) {
                log.debug("table Exists!");
            } else {
                admin.createTable(tableDesc);
                log.debug("create table Success!");
            }
        } catch (IOException e) {
            log.error(MessageFormat.format("Creating table {0} fail", tableName), e);
            return false;
        } finally {
            close(admin, null, null);
        }
        return true;
    }

    public List<String> getAllTableNames() {
        List<String> result = new ArrayList<>();
        try {
            TableName[] tableNames = admin.listTableNames();
            for (TableName tableName : tableNames) {
                result.add(tableName.getNameAsString());
            }
        } catch (IOException e) {
            log.error("Getting all tables' names fail", e);
        } finally {
            close(admin, null, null);
        }
        return result;
    }

    public Map<String, Map<String, String>> getResultScanner(String tableName) {
        Scan scan = new Scan();
        return this.queryData(tableName, scan);
    }

    private Map<String, Map<String, String>> queryData(String tableName, Scan scan) {
        Map<String, Map<String, String>> result = new HashMap<>();
        ResultScanner rs = null;
        Table table = null;
        try {
            table = getTable(tableName);
            rs = table.getScanner(scan);
            for (Result r : rs) {
                Map<String, String> columnMap = new HashMap<>();
                String rowKey = null;
                for (Cell cell : r.listCells()) {
                    if (rowKey == null) {
                        rowKey = Bytes.toString(cell.getRowArray(), cell.getRowOffset(), cell.getRowLength());
                    }
                    columnMap.put(
                            Bytes.toString(cell.getQualifierArray(), cell.getQualifierOffset(), cell.getQualifierLength()),
                            Bytes.toString(cell.getValueArray(), cell.getValueOffset(), cell.getValueLength()));
                }
                if (rowKey != null) {
                    result.put(rowKey, columnMap);
                }
            }
        } catch (IOException e) {
            log.error(MessageFormat.format("Getting all data in table failed, tableName:{0}", tableName), e);
        } finally {
            close(null, rs, table);
        }
        return result;
    }

    public String searchByRowKeyColumn(String tableName, String rowKey, String column){
        Table table = null;
        String res = "";
        try {
            table = getTable(tableName);
            Get get = new Get(rowKey.getBytes());
            get.addColumn("a".getBytes(), column.getBytes());
            Result result = table.get(get);
            res = result.listCells().stream().findAny().map(cell -> Bytes.toString(cell.getValueArray(), cell.getValueOffset(), cell.getValueLength())).orElse("");
        } catch (Exception e) {
            log.error(MessageFormat.format("search by row key and column fail, tableName:{0}, rowKey:{1}", tableName, rowKey), e);
        } finally {
            close(null, null, table);
        }
        return res;
    }

    public void putData(String tableName, String rowKey, String familyName, String[] columns, String[] values) {
        Table table = null;
        try {
            table = getTable(tableName);
            putData(table, rowKey, tableName, familyName, columns, values);
        } catch (Exception e) {
            log.error(MessageFormat.format("Adding or updating data in table fail, tableName:{0}, rowKey:{1}, familyName:{2}", tableName, rowKey, familyName), e);
        } finally {
            close(null, null, table);
        }
    }
    private void putData(Table table, String rowKey, String tableName, String familyName, String[] columns, String[] values) {
        try {
            //设置rowkey
            Put put = new Put(Bytes.toBytes(rowKey));
            if (columns != null && values != null && columns.length == values.length) {
                for (int i = 0; i < columns.length; i++) {
                    if (columns[i] != null && values[i] != null) {
                        put.addColumn(Bytes.toBytes(familyName), Bytes.toBytes(columns[i]), Bytes.toBytes(values[i]));
                    } else {
                        throw new NullPointerException(MessageFormat.format(
                                "column name and data cannot be empty, column:{0}, value:{1}", columns[i], values[i]));
                    }
                }
            }
            table.put(put);
            log.debug("putData add or update data Success,rowKey:" + rowKey);
            table.close();
        } catch (Exception e) {
            log.error(MessageFormat.format(
                    "putting data in table failed, tableName:{0}, rowKey:{1}, familyName:{2}",
                    tableName, rowKey, familyName), e);
        }
    }
    /**
     * 根据表名获取table
     */
    private Table getTable(String tableName) throws IOException {
        return connection.getTable(TableName.valueOf(tableName));
    }

    /**
     * 关闭流
     */
    private void close(Admin admin, ResultScanner rs, Table table) {
        if (admin != null) {
            try {
                admin.close();
            } catch (IOException e) {
                log.error("Closing admin failed", e);
            }
            if (rs != null) {
                rs.close();
            }
            if (table != null) {
                rs.close();
            }
            if (table != null) {
                try {
                    table.close();
                } catch (IOException e) {
                    log.error("Closing table failed", e);
                }
            }
        }
    }
}
