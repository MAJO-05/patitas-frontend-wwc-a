package pe.edu.cibertec.patitas_frontend_wc_a.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import pe.edu.cibertec.patitas_frontend_wc_a.client.AutenticacionClient;
import pe.edu.cibertec.patitas_frontend_wc_a.dto.LoginRequestDTO;
import pe.edu.cibertec.patitas_frontend_wc_a.dto.LoginResponseDTO;
import pe.edu.cibertec.patitas_frontend_wc_a.dto.LogoutRequestDTO;
import pe.edu.cibertec.patitas_frontend_wc_a.viewmodel.LoginModel;
import reactor.core.publisher.Mono;

@Controller
@RequestMapping("/login")
@CrossOrigin(origins = "http://localhost:5173")
public class LoginController {

    @Autowired
    WebClient webClientAutenticacion;

    @Autowired
    AutenticacionClient autenticacionClient;

    @GetMapping("/inicio")
    public String inicio(Model model) {
        LoginModel loginModel = new LoginModel("00", "", "", "", "");
        model.addAttribute("loginModel", loginModel);
        return "inicio";
    }

    @PostMapping("/autenticar")
    public String autenticar(@RequestParam("tipoDocumento") String tipoDocumento,
                             @RequestParam("numeroDocumento") String numeroDocumento,
                             @RequestParam("password") String password,
                             Model model) {

        // Validar campos de entrada
        if (tipoDocumento == null || tipoDocumento.trim().length() == 0 ||
                numeroDocumento == null || numeroDocumento.trim().length() == 0 ||
                password == null || password.trim().length() == 0) {

            LoginModel loginModel = new LoginModel("01", "Error: Debe completar correctamente sus credenciales", "", "", "");
            model.addAttribute("loginModel", loginModel);
            return "inicio";
        }

        try {
            // Invocar servicio de autenticación
            LoginRequestDTO loginRequestDTO = new LoginRequestDTO(tipoDocumento, numeroDocumento, password);
            Mono<LoginResponseDTO> monoLoginResponseDTO = webClientAutenticacion.post()
                    .uri("/login")
                    .body(Mono.just(loginRequestDTO), LoginRequestDTO.class)
                    .retrieve()
                    .bodyToMono(LoginResponseDTO.class);

            // Recuperar resultado de forma bloqueante (sincrona)
            LoginResponseDTO loginResponseDTO = monoLoginResponseDTO.block();

            if (loginResponseDTO.codigo().equals("00")) {
                LoginModel loginModel = new LoginModel("00", "", loginResponseDTO.nombreUsuario(), tipoDocumento, numeroDocumento);
                model.addAttribute("loginModel", loginModel);
                return "principal";
            } else {
                LoginModel loginModel = new LoginModel("02", "Error: Autenticación fallida", "", "", "");
                model.addAttribute("loginModel", loginModel);
                return "inicio";
            }

        } catch (Exception e) {
            LoginModel loginModel = new LoginModel("99", "Error: Ocurrió un problema en la autenticación", "", "", "");
            model.addAttribute("loginModel", loginModel);
            System.out.println(e.getMessage());
            return "inicio";
        }
    }

    @PostMapping("/logout")
    public String logout(@RequestParam("tipoDocumento") String tipoDocumento,
                         @RequestParam("numeroDocumento") String numeroDocumento,
                         Model model) {
        LogoutRequestDTO logoutRequestDTO = new LogoutRequestDTO(tipoDocumento, numeroDocumento);
        try {
            webClientAutenticacion.post()
                    .uri("/logout")
                    .body(Mono.just(logoutRequestDTO), LogoutRequestDTO.class)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block(); // Puede ser asíncrono si prefieres

            model.addAttribute("loginModel", new LoginModel("00", "Sesión cerrada correctamente", "", "", ""));
            return "inicio";
        } catch (Exception e) {
            model.addAttribute("loginModel", new LoginModel("99", "Error al cerrar la sesión", "", "", ""));
            return "inicio";
        }
    }
    @PostMapping("/autenticar-feign")
    public String autenticarFeign(@RequestParam("tipoDocumento") String tipoDocumento,
                                  @RequestParam("numeroDocumento") String numeroDocumento,
                                  @RequestParam("password") String password,
                                  Model model) {

        System.out.println("Consuming with Feign Client!!!");
        // Validar campos de entrada
        if (tipoDocumento == null || tipoDocumento.trim().length() == 0 ||
                numeroDocumento == null || numeroDocumento.trim().length() == 0 ||
                password == null || password.trim().length() == 0) {

            LoginModel loginModel = new LoginModel("01", "Error: Debe completar correctamente sus credenciales", "", "", "");
            model.addAttribute("loginModel", loginModel);
            return "inicio";
        }

        try {
            // preparar request
            LoginRequestDTO loginRequestDTO = new LoginRequestDTO(tipoDocumento, numeroDocumento, password);

            // consumir servicio con Feign Client
            LoginResponseDTO loginResponseDTO = autenticacionClient.login(loginRequestDTO);

            // validar respuesta del servicio
            if (loginResponseDTO.codigo().equals("00")) {
                LoginModel loginModel = new LoginModel("00", "", loginResponseDTO.nombreUsuario(), loginRequestDTO.tipoDocumento(), loginRequestDTO.numeroDocumento());
                model.addAttribute("loginModel", loginModel);
                return "principal";
            } else {
                LoginModel loginModel = new LoginModel("02", "Error: Autenticación fallida", "", "", "");
                model.addAttribute("loginModel", loginModel);
                return "inicio";
            }
        } catch (Exception e) {
            LoginModel loginModel = new LoginModel("99", "Error: Ocurrió un problema en la autenticación: " + e.getMessage(), "", "", "");
            model.addAttribute("loginModel", loginModel);
            System.out.println(e.getMessage());
            return "inicio";
        }
    }


}