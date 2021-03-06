package com.imooc.dao;

import com.imooc.bean.Command;
import com.imooc.bean.CommandContent;
import com.imooc.db.DbAccess;
import org.apache.ibatis.session.SqlSession;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 与指令表对应的数据库方法
 */
public class CommandDao {
    /**
     * 通过mybatis访问数据库实现查询操作
     */
    public List<Command> queryCommandList(Map<String, Object> parameters) {
        List<Command> CommandList = new ArrayList<>();
        SqlSession sqlSession = null;
        try {
            sqlSession = DbAccess.getSqlSession();
            ICommand iCommand = sqlSession.getMapper(ICommand.class);
            CommandList = iCommand.queryCommandList(parameters);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (sqlSession != null) {
                sqlSession.close();
            }
        }
        return CommandList;
    }

    /**
     * 删除单条信息
     *
     * @param id 删除消息的id
     */
    public void deleteOne(int id) {
        SqlSession sqlSession = null;
        try {
            sqlSession = DbAccess.getSqlSession();
            ICommand iCommand = sqlSession.getMapper(ICommand.class);
            iCommand.deleteOne(id);
            sqlSession.commit();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (sqlSession != null) {
                sqlSession.close();
            }
        }
    }

    /**
     * 批量删除数据
     *
     * @param ids 想要删除的id列表
     */
    public void deleteBatch(List<Integer> ids) {
        SqlSession sqlSession = null;
        try {
            sqlSession = DbAccess.getSqlSession();
            ICommand iCommand = sqlSession.getMapper(ICommand.class);
            iCommand.deleteBatch(ids);
            sqlSession.commit();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (sqlSession != null) {
                sqlSession.close();
            }
        }
    }

    /**
     * 添加新的command内容
     *
     * @param command 添加的指令参数
     *                添加操作必须分开
     *                mybatis不像hibernate可以级联插入
     *                只能自己书写sql语句分开单独插入所有内容
     */
    public void addOne(Command command) {
        SqlSession sqlSession = null;
        try {
            sqlSession = DbAccess.getSqlSession();
            //先插入command数据，先提交，否则不满足外键约束，添加不了数据
            ICommand iCommand = sqlSession.getMapper(ICommand.class);
            iCommand.addOne(command);
            sqlSession.commit();

            //重新获取sqlsession，使得两次插入操作分开
            sqlSession = DbAccess.getSqlSession();
            List<CommandContent> contents = command.getContentList();
            //设置commandId
            for (CommandContent content : contents) {
                content.setCommandId(command.getId());
            }
            //再插入content数据
            ICommandContent iCommandContent = sqlSession.getMapper(ICommandContent.class);
            iCommandContent.addBatch(contents);
            //提交事务，完成添加
            sqlSession.commit();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (sqlSession != null) {
                sqlSession.close();
            }
        }
    }

    /**
     * 查询单个command对象
     *
     * @param id 查询条件
     * @return command对象
     */
    public Command queryOneById(int id) {
        Command command = new Command();
        SqlSession sqlSession = null;
        try {
            sqlSession = DbAccess.getSqlSession();
            ICommand iCommand = sqlSession.getMapper(ICommand.class);
            command = iCommand.queryOneById(id);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (sqlSession != null) {
                sqlSession.close();
            }
        }
        return command;
    }

    /**
     * 查询记录总条数
     *
     * @param command 指令对象
     * @return 总条数
     */
    public int count(Command command) {
        int countNum = 0;
        SqlSession sqlSession = null;
        try {
            sqlSession = DbAccess.getSqlSession();
            ICommand iCommand = sqlSession.getMapper(ICommand.class);
            //从数据库查询得到总条数
            countNum = iCommand.count(command);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (sqlSession != null) {
                sqlSession.close();
            }
        }
        return countNum;

    }


    //存在问题，暂停使用

    /**
     * 修改单个对象
     *
     * @param command 参数
     */
    public void modifyOne(Command command) {
        SqlSession sqlSession = null;
        try {
            //先修改指令
            sqlSession = DbAccess.getSqlSession();
            ICommand iCommand = sqlSession.getMapper(ICommand.class);
            iCommand.modifyOne(command);

            //先删除内容再重新添加
            //设置content中的外键（commandId）
            //否则添加不进数据库
            List<CommandContent> contents = command.getContentList();
            for (CommandContent content : contents) {
                content.setCommandId(command.getId());
            }

            // 删除command对应的所有内容，重新添加
            ICommandContent iCommandContent = sqlSession.getMapper(ICommandContent.class);
            iCommandContent.deleteBatch(command.getId());

            //重新添加数据
            iCommandContent.addBatch(command.getContentList());
            sqlSession.commit();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (sqlSession != null) {
                sqlSession.close();
            }
        }
    }
}
