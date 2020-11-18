package com.itheima.dianshang.controller;


import com.itheima.dianshang.controller.ViewObject.UserVo;
import com.itheima.dianshang.error.BusinessException;
import com.itheima.dianshang.error.EmBussinessError;
import com.itheima.dianshang.response.CommonReturnType;
import com.itheima.dianshang.service.impl.UserServiceImpl;
import com.itheima.dianshang.service.model.UserModel;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import sun.misc.BASE64Encoder;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

@Controller
@RequestMapping("/user")
public class UserController extends BaseController {

    @Autowired
    UserServiceImpl userService;


    @Autowired
    HttpServletRequest httpServletRequest; //单例的 内部使用ThreadLocal是线程安全的




    /*
    用户的登入
     */
    @GetMapping(value = "/login")
    @ResponseBody
    public CommonReturnType userLogin(@RequestParam("telphone") String telphone, @RequestParam("password") String password) throws BusinessException, UnsupportedEncodingException, NoSuchAlgorithmException {
        UserModel userModel = null;

        //先做入参的非空校验，方法任何时候都要做入参的非空校验，这才是严谨的编码
        if (StringUtils.isEmpty(telphone) || StringUtils.isEmpty(password)) {
            throw new BusinessException(EmBussinessError.PARAMETER_VALIDATION_ERROR);
        }


        userModel = userService.validateLogin(telphone, enCodeByMD5(password));

        //将登录凭证加入到合法用户的session中，将pojo类放入session域中
        this.httpServletRequest.getSession().setAttribute("IS_LOGIN", true);
        this.httpServletRequest.getSession().setAttribute("USER_LOG", userModel);
        System.out.println(userModel);
        //服务器调用成功，但是不需要返还给前端任何信息，不需要把查询出来的信息给前端。就给个null值即可
        return CommonReturnType.create(null);
    }

    /*
    用户的注册接口
     */
    @PostMapping(value = "/register")
    @ResponseBody
    public CommonReturnType register(@RequestBody UserModel userModel) throws BusinessException {
        System.out.println("=============");
        //判断otp和tel是否符合，就是判断你的验证码是否输入正确
        String sessionOpt = (String) httpServletRequest.getSession().getAttribute(userModel.getTelphone());

        //前端传来的otpCode
        String otpCode = userModel.getOtpCode();

        if (!sessionOpt.equals(otpCode)) {
            throw new BusinessException(EmBussinessError.PARAMETER_VALIDATION_ERROR);
        }

        //前端的pwd
        String pwd = userModel.getPassword();
        try {
            userModel.setEncrptPassword(this.enCodeByMD5(pwd));
        } catch (Exception e) {
            e.printStackTrace();
        }
        userModel.setRegisterMode("byphone");
        System.out.println("=====" + userModel + "=====");
        userService.registers(userModel);
        CommonReturnType commonReturnType = CommonReturnType.create(null);
        return commonReturnType;
    }

    //对密码进行md5的加密
    public String enCodeByMD5(String password) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        BASE64Encoder base64Encoder = new BASE64Encoder();
        String newStr = base64Encoder.encode(md5.digest(password.getBytes("utf-8")));
        return newStr;
    }





    //用户短信验证码
    @RequestMapping(value = "/getotp")
    @ResponseBody
    public CommonReturnType getOtp(@RequestParam(name = "telphone") String telphone) {
        //生成opt验证码，我的验证码是6位的
        //先生成一个随机数种子
        Random random = new Random();
        //randomInt的取值范围是0到899999
        int randomInt = random.nextInt(899999);
        //现在randomInt的取值范围是0+100000=100000到899999+100000=999999
        randomInt += 100000;
        //将数字转换成字符串类型，千万别忘了
        String optCode = String.valueOf(randomInt);

        //将手机号和opt进行绑定，其实就是将验证码放入session域中
        httpServletRequest.getSession().setAttribute(telphone, optCode);

        //接下来需要调用第三方短信接口，将验证码发送到用户手机上，这一步不做了，暂时用输出代替他
        System.out.println("telphone" + telphone + ":optCode" + optCode);
        return CommonReturnType.create(null);
    }


    @PostMapping(value = "/getUser")
    @ResponseBody
    public CommonReturnType getUserById(@RequestParam(name = "id") Integer id) throws BusinessException {
        /*
        一般用户的信息不能全部显示给前端页面，所以在建义ModelVo来显示要传给前端的数据
         */

        UserModel userModel = userService.getUserById(id);




        if (userModel == null) {
                //抛出一个自定义的异常

                throw new BusinessException(EmBussinessError.USER_NOT_LOGIN);
        }


        UserVo userVo = new UserVo();
        if (userModel == null) {
            return null;
        } else {
            BeanUtils.copyProperties(userModel, userVo);
        }
        return CommonReturnType.create(userVo);

    }


}
