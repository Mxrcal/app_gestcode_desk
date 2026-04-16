/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cat.xtec.ioc.demo_aplicacio_escriptori.api;

        /**
     * Classe per encapsular el resultat HTTP (codi i cos)
     */
    public class HttpResult {
        public int statusCode;
        public String body;
        public HttpResult(int statusCode, String body) {
            this.statusCode = statusCode;
            this.body = body;
        }
    }

