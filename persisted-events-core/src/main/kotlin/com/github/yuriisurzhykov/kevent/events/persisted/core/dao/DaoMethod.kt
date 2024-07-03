package com.github.yuriisurzhykov.kevent.events.persisted.core.dao

/**
 * The DaoMethod enum represents the available methods for a DAO (Data Access Object).
 * This enum is used only for symbol processor, to generate different DAO methods for
 * the auto-generated entity.
 *
 * @property INSERT Represents the "insert" method in a DAO.
 * @property INSERT_ALL Represents the "insertAll" method in a DAO.
 * @property DELETE Represents the "delete" method in a DAO.
 * @property DELETE_BY_KEY Represents the "deleteByKey" method in a DAO.
 * @property UPDATE Represents the "update" method in a DAO.
 * @property UPSERT Represents the "upsert" method in a DAO.
 * @property CLEAR_TABLE Represents the "clearTable" method in a DAO.
 * @property GET_ALL Represents the "getAll" method in a DAO.
 * @property GET_BY_KEY Represents the "getByKey" method in a DAO.
 * @property GET_LIMIT_1 Represents the "getLast" method in a DAO.
 */
enum class DaoMethod(val methodName: String) {
    INSERT("insert"),
    INSERT_ALL("insertAll"),
    DELETE("delete"),
    DELETE_BY_KEY("deleteByKey"),
    UPDATE("update"),
    UPSERT("upsert"),
    CLEAR_TABLE("clearTable"),
    GET_ALL("getAll"),
    GET_BY_KEY("getByKey"),
    GET_LIMIT_1("getLast")
}