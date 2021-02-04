﻿package probono.controller;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;

import probono.model.ProbonoService;
import probono.model.dto.ActivistDTO;

@Slf4j
@WebServlet("/probono")
public class ProbonoFrontController extends HttpServlet {
	public ProbonoFrontController() {
		super();
	}
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		process(request, response);
	}
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		process(request, response);
	}
	protected void process(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("utf-8");
		//command pattern
		String command = request.getParameter("command");
		if(command == null) {
			response.sendRedirect("index.html");
			return;
		}
		try{
			if(command.equals("probonoProjectAll")){//모든 probono project 정보 검색
				probonoProjectAll(request, response);
			}else if(command.equals("activistAll")){//모든 재능 기부자 검색
				activistAll(request, response);
			}else if(command.equals("activist")){//특정 재능 기부자 정보 검색
				activist(request, response);
			}else if(command.equals("activistInsert")){//재능 기부자 추가 등록
				activistInsert(request, response);
			}else if(command.equals("activistUpdateReq")){//재능 기부자 정보 수정요청
				activistUpdateReq(request, response);
			}else if(command.equals("activistUpdate")){//재능 기부자 정보 수정
				activistUpdate(request, response);
			}else if(command.equals("activistDelete")){//재능 기부자 탈퇴[삭제]
				activistDelete(request, response);
			}
		}catch(Exception s){
			request.setAttribute("errorMsg", s.getMessage());
			log.info("에러 메세지" + s.getMessage());
			request.getRequestDispatcher("showError.jsp").forward(request, response);
			s.printStackTrace();
		}
	}
	//모두 ProbonoProject 검색 메소드
	public void probonoProjectAll(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String url = "showError.jsp";
		try {
			request.getSession().setAttribute("probonoProjectAll", ProbonoService.getAllProbonoProjects());
			url = "probonoProjectList.jsp";
		}catch(Exception s){
			log.info("에러 메세지 : 모든 probono project검색기록을 찾지 못했습니다.");
			request.setAttribute("errorMsg", s.getMessage());
			s.printStackTrace();
		}
		request.getRequestDispatcher(url).forward(request, response);

	}
	//???
	//모든 재능 기부자 검색 - 검색된 데이터 출력 화면[activistList.jsp]
	public void activistAll(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String url = "showError.jsp";
		try {
			request.getSession().setAttribute("activistAll", ProbonoService.getAllActivists());
			url = "activistList.jsp";
		}catch(Exception s){
			log.info("에러 메세지 : 모든 재능 기부자 검색 결과를 찾지 못했습니다.");
			request.getSession().getAttribute("errorMsg");
			s.printStackTrace();
		}
		request.getRequestDispatcher(url).forward(request, response);
	}
	//재능 기부자 검색
	public void activist(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String url = "showError.jsp";
		try {
			request.getSession().setAttribute("activist", ProbonoService.getActivist(request.getParameter("activistId")));
			url = "activistDetail.jsp";
		}catch(Exception s){
			log.info("에러 메세지 : 특정 기부자를 찾을 수 없습니다.");
			request.setAttribute("errorMsg", s.getMessage());
			s.printStackTrace();
		}
		request.getRequestDispatcher(url).forward(request, response);
	}
	//재능 기부자 가입 메소드
	protected void activistInsert(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String url = "showError.jsp";
		String id = request.getParameter("id");
		String name = request.getParameter("name");
		String pw = request.getParameter("pw");
		String major = request.getParameter("major");
		//해킹등으로 불합리하게 요청도 될수 있다는 가정하에 모든 데이터가 제대로 전송이 되었는지를 검증하는 로직
		if(id != null && id.length() !=0 && name != null) {
			ActivistDTO activist = new ActivistDTO(id, name, pw, major);
			try{
				boolean result = ProbonoService.addActivist(activist);
				if(result){
					request.getSession().setAttribute("activist", activist);
					request.setAttribute("successMsg", "가입 완료");
					log.info("activist 가입 완료");
					url = "activistDetail.jsp";
					ActivistDTO dto = (ActivistDTO)request.getSession().getAttribute("dto");
					dto.setMajor(request.getParameter("major").trim());
					dto.setPassword(request.getParameter("password").trim());
				}else{
					request.setAttribute("errorMsg", "다시 시도하세요");
					log.info("activist 가입 다시 시도하세요.");
				}


			}catch(Exception s){
				log.info("activist 회원가입 중 에러 발생 : " + s.getMessage());
				request.setAttribute("errorMsg", s.getMessage());
			} 
		} else {
			request.setAttribute("errorMsg", "회원님의 정보가 제대로 입력되지 않았습니다.");
		}
		request.getRequestDispatcher(url).forward(request, response);
	}
	//재능 기부자 수정 요구
	public void activistUpdateReq(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String url = "showError.jsp";
		try {
			request.getSession().setAttribute("activist", ProbonoService.getActivist(request.getParameter("activistId")));
			url = "activistUpdate.jsp";
		}catch(Exception s){
			log.info("activist update 중 에러 발생 : " + s.getMessage());
			request.setAttribute("errorMsg", s.getMessage());
			s.printStackTrace();
		}
		request.getRequestDispatcher(url).forward(request, response);
	}
	//???
	//재능 기부자 수정 - 상세정보 확인 jsp[activistDetail.jsp]
	public void activistUpdate(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String url = "showError.jsp";
		String id = request.getParameter("activistId");
		String major = request.getParameter("major");
		try{
			boolean result = ProbonoService.updateActivist(id,major);
			if(result){
				request.getSession().setAttribute("activist", ProbonoService.getActivist(id));
				request.getSession().setAttribute("successMsg", "수정 완료");
				log.info("재능 기부자 수정이 완료되었습니다.");
				url = "activistDetail.jsp";
			}else{
				request.setAttribute("errorMsg", "다시 시도하세요");
				log.info("재능 기부자 수정  다시 시도해주세요.");
			}
		}catch(Exception s){
			log.info("재능 기부자 수정 중 에러 발생 : " + s.getMessage());
			request.setAttribute("errorMsg", s.getMessage());
			s.printStackTrace();
		}
		request.getRequestDispatcher(url).forward(request, response);
	}
	//???
	//재능 기부자 삭제
	public void activistDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String url = "showError.jsp";
		try {
			boolean result = ProbonoService.deleteActivist(request.getParameter("activistId"));
			if(result){
				request.getSession().setAttribute("activist", result);
				request.getSession().setAttribute("successMsg", "삭제 완료");
				log.info("재능 기부자 삭제 완료되었습니다.");
				url = "index.html";
			}else{
				request.setAttribute("errorMsg", "다시 시도하세요");
				log.info("재능 기부자 삭제를 다시 시도해주세요.");
			}
		}catch(Exception s){
			log.info("재능 기부자 삭제 중 에러 발생 : " + s.getMessage());
			request.setAttribute("errorMsg", s.getMessage());
			s.printStackTrace();
		}
		request.getRequestDispatcher(url).forward(request, response);
	}
}