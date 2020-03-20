package dao;

import common.JavaImageServerException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ImageDao {
    //把image对象插入到数据库中
    public void insert(Image image){
         //1.获取数据库连接
        Connection connection=DBUtil.getConnection();
        //2.创建并拼装SQL语句
        String sql="insert into image_table values(null,?,?,?,?,?,?) ";
        PreparedStatement statement=null;
        try {
            statement=connection.prepareStatement(sql);
            statement.setString(1,image.getImageName());
            statement.setInt(2,image.getSize());
            statement.setString(3,image.getUploadTime());
            statement.setString(4,image.getContentType());
            statement.setString(5,image.getPath());
            statement.setString(6,image.getMd5());
            //3.执行SQL语句
            int ret=statement.executeUpdate();
            if(ret!=1){
                 throw new JavaImageServerException("插入数据库错误!");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }catch (JavaImageServerException e){
            e.printStackTrace();
        }
        finally {
            //4.关闭连接和statement对象
            DBUtil.close((com.mysql.jdbc.Connection) connection,statement,null);
        }


    }
    //查找数据库所有图片的信息
    public List<Image> selectAll(){
        List<Image> list=new ArrayList<>();
        //1.获取数据库连接
        Connection connection=DBUtil.getConnection();
        //2.构造sql语句
        String sql="select *from image_table";
        PreparedStatement statement=null;
        ResultSet resultSet=null;
        //3.执行sql语句
        try {
            statement=connection.prepareStatement(sql);
             resultSet=statement.executeQuery();

            //4.处理结果集
            while(resultSet.next()){
                Image image =new Image();
                image.setImageId(resultSet.getInt("imageId"));
                image.setImageName(resultSet.getString("imageName"));
                image.setSize(resultSet.getInt("size"));
                image.setUploadTime(resultSet.getString("uploadTime"));
                image.setContentType(resultSet.getString("contentType"));
                image.setPath(resultSet.getString("path"));
                image.setMd5(resultSet.getString("md5"));
                list.add(image);
            }
            return list;
        } catch (SQLException e) {
            e.printStackTrace();

        }
        finally {
            //5.关闭连接
        DBUtil.close((com.mysql.jdbc.Connection) connection,statement,resultSet);
        }
        return null;
    }
    //查找指定图片信息
    public Image selectOne(int imageId){
        //1.获取数据库连接
        Connection connection=DBUtil.getConnection();

        //2.构造SQL语句
        String sql="select * from image_table where imageId = ?";
        PreparedStatement statement=null;
        ResultSet resultSet=null;


        try {
            //3.执行SQL语句
            statement=connection.prepareStatement(sql);
            statement.setInt(1,imageId);
            resultSet =statement.executeQuery();
            //4.处理结果集
            if(resultSet.next()){
            Image image =new Image();
            image.setImageId(resultSet.getInt("imageId"));
            image.setImageName(resultSet.getString("imageName"));
            image.setSize(resultSet.getInt("size"));
            image.setUploadTime(resultSet.getString("uploadTime"));
            image.setContentType(resultSet.getString("contentType"));
            image.setPath(resultSet.getString("path"));
            image.setMd5(resultSet.getString("md5"));
            return  image;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        finally {
            //5.关闭连接
            DBUtil.close((com.mysql.jdbc.Connection) connection,statement,resultSet);
        }


        return null;
    }
    //根据imageId删除指定的图片
    public void delete(int imageId){
        //1.获取数据库连接
      Connection connection=DBUtil.getConnection();

      //2.拼装SQL语句
      String sql ="delete from image_table where imageId = ?";
      PreparedStatement statement=null;
        //3.执行SQL语句
        try {
            statement=connection.prepareStatement(sql);
            statement.setInt(1,imageId);
            int ret= statement.executeUpdate();
            if(ret!=1){
                throw new JavaImageServerException("删除数据库操作失败");
            }
        } catch (SQLException | JavaImageServerException e) {
            e.printStackTrace();
        }finally {
            //4.关闭连接
            DBUtil.close((com.mysql.jdbc.Connection) connection,statement,null);
        }


    }
//如果数据库在云服务器上，需要把程序部署到云服务器上才能运行看到效果
   public static void main(String[] args) {
        //用于进行简单的测试
        //1.测试插入数据
//        Image image=new Image();
//        image.setImageName("1.png");
//        image.setSize(100);
//        image.setUploadTime("20200302");
//        image.setContentType("image/png");
//        image.setPath("./data/1.png");
//        image.setMd5("11223344");
//        ImageDao imageDao=new ImageDao();
//        imageDao.insert(image);
        //2.测试查找所有图片的信息
//        ImageDao imageDao=new ImageDao();
//        List<Image> list=imageDao.selectAll();
//        System.out.println(list);
      //3.测试查找指定图片信息
//        ImageDao imageDao=new ImageDao();
//        Image image =imageDao.selectOne(5);
//        System.out.println(image);
        //4.测试删除指定图片信息
//        ImageDao imageDao=new ImageDao();
//        imageDao.delete(1);
    }
}
