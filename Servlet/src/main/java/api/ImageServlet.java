package api;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dao.Image;
import dao.ImageDao;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ImageServlet extends HttpServlet {
    //既能查看所有，又能查看指定
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //考虑到查看所有图片属性和查看指定图片属性
        //通过是否URL中带有ImageId参数来进行区分
        //存在imageId查看指定图片属性，否则就查看所有图片属性
        //例如： URL /image？imageId=100
        //如果URL中不存在imageId那么返回null
        String imageId=req.getParameter("imageId");
        if(imageId==null|| imageId.equals("")){
            //查看所有图片属性
             seletcAll(req,resp);
        }
        else{
            //查看指定图片
            selectOne(imageId,resp);
        }
    }

    private void selectOne(String imageId, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json; charset=utf-8");
        //1.创建一个ImageDao对象
        ImageDao imageDao=new ImageDao();
        Image image= imageDao.selectOne(Integer.parseInt(imageId));
        //2.使用json把查到的数据转成json格式，并写回给响应对象
        Gson gson =new GsonBuilder().create();
        String jsonData=gson.toJson(image);
        resp.getWriter().write(jsonData);
    }

    private void seletcAll(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json; charset=utf-8");
        //1.创建一个ImageDao对象
        ImageDao imageDao=new ImageDao();
        List<Image> images=imageDao.selectAll();
        //2.把查找的结果转换成JSON格式的字符串，并且会给resp对象
         Gson gson=new GsonBuilder().create();
         //jsonData就是一个json格式的字符串了，就和之前约定的格式是一样的了
        //gson帮我们自动完成了大量的格式转换
        String jsonData=gson.toJson(images);
        resp.getWriter().write(jsonData);
    }

    //上传图片
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
         //1.获取图片属性信息，并且存入数据库
        //a)需要创建一个factory对象和upload对象
          FileItemFactory factory=new DiskFileItemFactory();
          ServletFileUpload upload=new ServletFileUpload(factory);
          //b)通过upload对象进一步请求解析（解析http请求中奇怪的body的内容）
                  //FileItem代表一个上传的文件对象
                  //理论上来说，http支持一个请求中同时上传多个文件
        List<FileItem> items=null;
        try {
            items =upload.parseRequest(req);
        } catch (FileUploadException e) {
            //出现异常说明解析出错
            e.printStackTrace();
            //告诉客户端出现的具体错误
            resp.setContentType("application/json; charset=utf-8");
            resp.getWriter().write("{\"ok\" : false , \"reason\" : \"请求解析失败\"}");
         return;
        }
        //c)把FileItem中的属性提取出来，转换成Image对象，才能存到数据库中
          //当前只考虑一张图片的情况
          FileItem fileItem=items.get(0);
          Image image=new Image();
          image.setImageName(fileItem.getName());
          image.setSize((int)fileItem.getSize());
          //手动获取当前日期,并转换成格式化日期
        SimpleDateFormat simpleDateFormat =new SimpleDateFormat("yyyyMMdd");

          image.setUploadTime(simpleDateFormat.format(new Date()));
        image.setContentType(fileItem.getContentType());
        //自己构造一个路径保存,引入时间戳是为了让文件路径能够唯一
        image.setMd5(DigestUtils.md5Hex(fileItem.get()));
        image.setPath("./image/"+image.getMd5());

        //存到数据库中
        ImageDao imageDao=new ImageDao();
        //看看数据库中是否存在相同的MD5值的图片，不存在，返回null
        Image existImage=imageDao.selectByMd5(image.getMd5());
        imageDao.insert(image);
        //2.获取图片的内容信息，并且写入磁盘文件
        if(existImage==null){
        File file =new File(image.getPath());
        try {
            fileItem.write(file);
        } catch (Exception e) {
            e.printStackTrace();
            resp.setContentType("application/json; charset=utf-8");
            resp.getWriter().write("{\"ok\" : false , \"reason\" : \"写磁盘失败\"}");
            return;
        }
        }
        //3.给客户端返回结果数据
//         resp.setContentType("application/json; charset=utf-8");
//         resp.getWriter().write("{\"ok\" :true}");
        resp.sendRedirect("index.html");
    }
//删除图片
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        resp.setContentType("application/json; charset=utf-8");
        //1.先获取到请求中的imageId
        String imageId=req.getParameter("imageId");
        if(imageId==null||imageId.equals("")){
            resp.setStatus(200);
            resp.getWriter().write("{\"ok\" : false , \"reason\" : \"解析请求出错\"}");

        }
        //2.创建ImageDao对象，查看到该图片d对象对应的相关属性
        ImageDao imageDao=new ImageDao();
        Image image=imageDao.selectOne(Integer.parseInt(imageId));
        if(image==null){
            //此时请求中传入的id在数据中不存在
            resp.setStatus(200);
            resp.getWriter().write("{\"ok\" : false , \"reason\" : \"imageId 在数据库中不存在\"}");

        }
        //3.删除数据库中的记录
        Image existImage=imageDao.selectByMd5(image.getMd5());
        imageDao.delete(Integer.parseInt(imageId));

        //4.删除本地磁盘文件
       if(existImage==null) {
           File file = new File(image.getPath());
           file.delete();
           resp.setStatus(200);
           resp.getWriter().write("{\"ok\" : true }");

       }

    }
}
