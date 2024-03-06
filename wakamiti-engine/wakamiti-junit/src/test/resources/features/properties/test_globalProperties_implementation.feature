# language: es
@implementation
Característica: Pruebas de propiedades dinámicas 2

  # redefinition.stepMap: 30
  @ID-1
  Escenario: Escenario tipo número
    * un número entero 6
    * un número entero ${number.integer}
    * un número entero ${1#}
    * un número decimal 3,2
    * un número decimal ${number.decimal}
    * un número decimal ${4#}
    * un número int 6
    * un número int ${number.integer}
    * un número int ${7#}
    * un número short 6
    * un número short ${number.integer}
    * un número short ${10#}
    * un número long 6
    * un número long ${number.integer}
    * un número long ${13#}
    * un número biginteger 6
    * un número biginteger ${number.integer}
    * un número biginteger ${16#}
    * un número byte 6
    * un número byte ${number.integer}
    * un número byte ${19#}
    * un número double 3,2
    * un número double ${number.decimal}
    * un número double ${22#}
    * un número float 3,2
    * un número float ${number.decimal}
    * un número float ${25#}
    * un número bigdecimal 3,2
    * un número bigdecimal ${number.decimal}
    * un número bigdecimal ${28#}

  # redefinition.stepMap: 13
  @ID-2
  Escenario: Escenario tipo fecha y tiempo
    * ahora son las 10:05
    * ahora son las 10:05:03
    * ahora son las 10:05:03.123
    * ahora son las ${datetime.now}
    * ahora son las ${3#}
    * hoy es 2023-01-10
    * hoy es ${datetime.today}
    * hoy es ${6#}
    * este instante es 2023-01-10T10:05
    * este instante es 2023-01-10T10:05:03
    * este instante es 2023-01-10T10:05:03.123
    * este instante es ${datetime.today}T${datetime.now}
    * este instante es ${10#}

  # redefinition.stepMap: 12
  @ID-3
  Escenario: Escenario tipo string
    * un string 'ABC aa'
    * un string '${text} aa'
    * un string '${1#}'
    * un texto 'ABC aa'
    * un texto '${text} aa'
    * un texto '${4#}'
    * una palabra ABC
    * una palabra ${text}
    * una palabra ${7#}
    * un id ABC
    * un id ${text}
    * un id ${10#}

  # redefinition.stepMap: 3
  @ID-4
  Escenario: Escenario tipo fichero
    * un fichero 'src/test/resources/features/properties/ABC'
    * un fichero 'src/test/resources/features/properties/${text}'
    * un fichero '${1#}'

  # redefinition.stepMap: 4
  @ID-5
  Escenario: Escenario tipo URL
    * una URL https://test.es/ABC
    * una URL ${url}
    * una URL https://${host}/${text}
    * una URL ${1#}