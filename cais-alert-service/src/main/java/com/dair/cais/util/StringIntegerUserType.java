package com.dair.cais.util;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.usertype.UserType;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

public class StringIntegerUserType implements UserType<Integer> {

    @Override
    public int getSqlType() {
        return Types.VARCHAR;
    }

    @Override
    public Class<Integer> returnedClass() {
        return Integer.class;
    }

    @Override
    public boolean equals(Integer x, Integer y) {
        if (x == y) {
            return true;
        }
        if (x == null || y == null) {
            return false;
        }
        return x.equals(y);
    }

    @Override
    public int hashCode(Integer x) {
        return x.hashCode();
    }

    @Override
    public Integer nullSafeGet(ResultSet rs, int position, SharedSessionContractImplementor session, Object owner) throws SQLException {
        String value = rs.getString(position);
        if (value == null) {
            return null;
        }
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException e) {
            throw new HibernateException("Unable to convert String to Integer: " + value, e);
        }
    }

    @Override
    public void nullSafeSet(PreparedStatement st, Integer value, int index, SharedSessionContractImplementor session) throws SQLException {
        if (value == null) {
            st.setNull(index, Types.VARCHAR);
        } else {
            st.setString(index, value.toString());
        }
    }

    @Override
    public Integer deepCopy(Integer value) {
        return value;
    }

    @Override
    public boolean isMutable() {
        return false;
    }

    @Override
    public Serializable disassemble(Integer value) {
        return value;
    }

    @Override
    public Integer assemble(Serializable cached, Object owner) {
        return (Integer) cached;
    }

    @Override
    public Integer replace(Integer detached, Integer managed, Object owner) {
        return detached;
    }
}