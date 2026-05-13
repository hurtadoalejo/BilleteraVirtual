package com.proyectofinal.billeteravirtual.model;

public enum CodigoResultadoTransaccion {
    SIN_ERROR,
    SALDO_INSUFICIENTE,
    USUARIO_NO_ENCONTRADO,
    BILLETERA_ORIGEN_NO_ENCONTRADA,
    BILLETERA_DESTINO_NO_ENCONTRADA,
    MISMA_BILLETERA,
    VALOR_INVALIDO,
    ERROR_DESCONOCIDO
}