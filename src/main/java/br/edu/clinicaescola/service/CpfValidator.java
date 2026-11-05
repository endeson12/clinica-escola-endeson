package br.edu.clinicaescola.service;

/** Validacao de CPF brasileiro pelos digitos verificadores. */
public final class CpfValidator {

    private CpfValidator() {
    }

    /** Remove mascara (pontos, traco e espacos), devolvendo apenas digitos. */
    public static String normalizar(String cpf) {
        return cpf == null ? "" : cpf.replaceAll("\\D", "");
    }

    public static boolean isValido(String cpf) {
        String c = normalizar(cpf);
        if (c.length() != 11) return false;
        if (c.chars().distinct().count() == 1) return false; // 000.000.000-00, 111..., etc.
        int d1 = digito(c, 9, 10);
        int d2 = digito(c, 10, 11);
        return d1 == c.charAt(9) - '0' && d2 == c.charAt(10) - '0';
    }

    private static int digito(String c, int qtd, int pesoInicial) {
        int soma = 0;
        for (int i = 0; i < qtd; i++) {
            soma += (c.charAt(i) - '0') * (pesoInicial - i);
        }
        int resto = (soma * 10) % 11;
        return resto == 10 ? 0 : resto;
    }
}
