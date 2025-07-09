# language: es
@implementation @modbustest
Característica: Test de modbus


  @ID-modbustest01
  Escenario: Test de modbus
    Cuando se leen 2 registros a partir de la posición 1
    Entonces los registros leídos contienen el valor 1
    Y los registros leídos contienen el valor 2

  @ID-modbustest02
  Escenario: Test de modbus - write
    Dado se escribe el valor 10 en la posición 1
    Cuando se leen 1 registros a partir de la posición 1
    Entonces los registros leídos contienen el valor 10