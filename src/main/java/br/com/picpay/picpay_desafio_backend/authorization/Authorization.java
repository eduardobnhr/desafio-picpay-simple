package br.com.picpay.picpay_desafio_backend.authorization;

public record Authorization(
    String message
) {
    public boolean isAuthorized(){
        return message.equals("Autorizado");
    }
}
