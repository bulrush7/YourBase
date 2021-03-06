/*
 * Copyright 2011 The Apache Software Foundation
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.codefollower.yourbase.test.jdbc.ddl;

import static org.junit.Assert.assertTrue;

import java.sql.SQLException;

import org.junit.Assert;
import org.junit.Test;

import com.codefollower.yourbase.test.jdbc.TestBase;

public class CreateTableTest extends TestBase {
    @Test
    public void run() throws Exception {
        try {
            createTableSQL("CREATE TABLE IF NOT EXISTS CreateTableTest1 (f1 int, f2 long)");
            Assert.fail("not throw SQLException");
        } catch (SQLException e) {
            assertTrue(e.getMessage().contains("not found a primary key"));
        }

        createTableSQL("CREATE TABLE IF NOT EXISTS CreateTableTest2 (f1 int primary key, f2 long)");
        stmt.executeUpdate("INSERT INTO CreateTableTest2(f1, f2) VALUES(1, 2)");
        stmt.executeUpdate("INSERT INTO CreateTableTest2(f1, f2) VALUES(2, 3)");
        stmt.executeUpdate("INSERT INTO CreateTableTest2(f1, f2) VALUES(3, 4)");
        sql = "SELECT * FROM CreateTableTest2";
        printResultSet();

        try {
            //f3未定义
            stmt.executeUpdate("INSERT INTO CreateTableTest2(f1, f2, f3) VALUES(1, 2, 3)");
            Assert.fail("not throw SQLException");
        } catch (SQLException e) {
            assertTrue(e.getMessage().contains("not found"));
        }
        
        stmt.executeUpdate("DROP TABLE IF EXISTS CreateTableTest2");
    }
}
