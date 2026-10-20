package com.example.smartfactoryservice.Domain.Vo.Person;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 大屏人事模块视图对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PersonnelVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 总体出勤数据 */
    private AttendanceSummary attendance;

    /** 各部门出勤数据 */
    private List<DepartmentAttendance> departments;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AttendanceSummary implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        /** 上班人数 */
        private int working;
        /** 请假人数 */
        private int leave;
        /** 下班人数 */
        private int offWork;
        /** 总人数 */
        private int total;
        /** 出勤率 */
        private int rate;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DepartmentAttendance implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        /** 部门名称 */
        private String name;
        /** 上班人数 */
        private int working;
        /** 部门总人数 */
        private int total;
        /** 出勤率 */
        private int rate;
    }
}
