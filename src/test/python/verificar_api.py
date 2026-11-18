"""Verifica la API real de AutoAndes. Ejecutar con Python 3 y el backend activo.
Los registros temporales usan identificadores propios y se eliminan al terminar.
No borra ni altera las semillas del examen. La orden creada se conserva anulada.
"""
import base64
import datetime
import hashlib
import hmac
import json
import os
from pathlib import Path
import time
import urllib.request
import urllib.error

BASE = os.environ.get('AUTOANDES_API', 'http://localhost:8080/api/v1')
results = []

def call(name, method, route, expected, data=None, token=None, headers=None):
    request_headers = {'Content-Type': 'application/json', **(headers or {})}
    if token:
        request_headers['Authorization'] = 'Bearer ' + token
    request = urllib.request.Request(BASE + route, method=method,
        data=None if data is None else json.dumps(data).encode(), headers=request_headers)
    try:
        response = urllib.request.urlopen(request, timeout=30)
    except urllib.error.HTTPError as error:
        response = error
    with response:
        status = response.code
        raw = response.read()
        body = json.loads(raw) if raw else None
        response_headers = dict(response.headers)
    assert status == expected, (name, status, body)
    if status >= 400:
        assert {'timestamp', 'status', 'error', 'mensaje', 'ruta'} <= body.keys(), (name, body)
        assert body['status'] == status
    results.append({'caso': name, 'status': status, 'correcto': True})
    print(name, status, 'OK')
    return body, response_headers

tokens = {}
for user in ('admin', 'asesor', 'jefe'):
    auth, _ = call('Login ' + user, 'POST', '/auth/login', 200,
        {'username': user, 'password': 'Auto2026*'})
    assert auth['expiraEnSegundos'] == 3600
    assert 'password' not in auth
    tokens[user] = auth['token']
admin, asesor, jefe = (tokens[u] for u in ('admin', 'asesor', 'jefe'))
rejections = []
for user, password in [('inactivo', 'Auto2026*'), ('desconocido', 'Auto2026*'), ('admin', 'incorrecta')]:
    body, _ = call('Credenciales rechazadas ' + user, 'POST', '/auth/login', 401,
        {'username': user, 'password': password})
    rejections.append(body['mensaje'])
assert len(set(rejections)) == 1
call('Login sin datos', 'POST', '/auth/login', 400, {})
call('Sin token', 'GET', '/clientes', 401)
call('Token alterado', 'GET', '/clientes', 401, token=admin[:-10] + 'xxxxxxxxxx')
local = Path(__file__).resolve().parents[3] / '.local/application.properties'
properties = dict(line.split('=', 1) for line in local.read_text(encoding='utf-8').splitlines() if '=' in line)
claims = json.loads(base64.urlsafe_b64decode(admin.split('.')[1] + '=='))
claims['iat'] = int(time.time()) - 7200
claims['exp'] = int(time.time()) - 3600
encode = lambda value: base64.urlsafe_b64encode(value).rstrip(b'=').decode()
unsigned = admin.split('.')[0] + '.' + encode(json.dumps(claims).encode())
expired = unsigned + '.' + encode(hmac.new(base64.b64decode(properties['app.jwt.secret']), unsigned.encode(), hashlib.sha256).digest())
call('Token vencido con firma valida', 'GET', '/clientes', 401, token=expired)
call('Usuario actual', 'GET', '/auth/me', 200, token=admin)
clientes, _ = call('Clientes semilla', 'GET', '/clientes', 200, token=asesor)
assert len(clientes) == 6
servicios, _ = call('Servicios semilla', 'GET', '/servicios', 200, token=asesor)
assert all(any(s['codigo'] == f'SRV-{i:03}' for s in servicios) for i in range(1, 9))
assert next(s for s in servicios if s['codigo'] == 'SRV-008')['activo'] is False
call('Asesor no mantiene servicios', 'POST', '/servicios', 403, {}, asesor)
call('Asesor no ve reportes', 'GET', '/reportes/ingresos-por-servicio', 403, token=asesor)
call('Asesor no anula', 'PATCH', '/ordenes/1/anular', 403, {'motivo': 'Cliente solicita anular'}, asesor)
call('Jefe no registra ordenes', 'POST', '/ordenes', 403, {}, jefe)
call('Jefe no modifica vehiculos', 'PUT', '/vehiculos/1', 403, {}, jefe)
call('Servicio inválido', 'POST', '/servicios', 400, {'codigo': 'abc'}, admin)
call('Servicio inexistente', 'GET', '/servicios/999999999', 404, token=admin)
call('RN01 duplicado', 'POST', '/servicios', 409,
    {'codigo': 'SRV-001', 'nombre': 'Duplicado', 'precio': 50, 'duracionMin': 30, 'activo': True}, admin)
call('RN01 servicio usado', 'DELETE', '/servicios/1', 409, token=admin)
call('RN02 placa repetida', 'POST', '/vehiculos', 409,
    {'placa': 'AAA-111', 'marca': 'Toyota', 'modelo': 'Corolla', 'anio': 2020, 'clienteId': 1}, asesor)
call('RN02 cliente inexistente', 'POST', '/vehiculos', 404,
    {'placa': 'ZZZ-999', 'marca': 'Toyota', 'modelo': 'Corolla', 'anio': 2020, 'clienteId': 99999999}, asesor)
call('RN02 vehiculo usado', 'DELETE', '/vehiculos/1', 409, token=asesor)
before, _ = call('Conteo antes de rechazo', 'GET', '/ordenes', 200, token=asesor)
bad = {'vehiculoId': 1, 'kilometraje': 45000, 'detalles': [
    {'servicioId': 1, 'cantidad': 1}, {'servicioId': 2, 'cantidad': 1}, {'servicioId': 8, 'cantidad': 1}]}
call('RN03 tercer detalle inactivo', 'POST', '/ordenes', 409, bad, asesor)
after, _ = call('Sin cabecera parcial tras rechazo', 'GET', '/ordenes', 200, token=asesor)
assert len(before) == len(after)
call('RN03 sin detalles', 'POST', '/ordenes', 400,
    {'vehiculoId': 1, 'kilometraje': 45000, 'detalles': []}, asesor)
call('RN03 servicio repetido', 'POST', '/ordenes', 400,
    {'vehiculoId': 1, 'kilometraje': 45000, 'detalles': [{'servicioId': 1, 'cantidad': 1}, {'servicioId': 1, 'cantidad': 1}]}, asesor)
call('RN04 kilometraje anterior', 'POST', '/ordenes', 409,
    {'vehiculoId': 1, 'kilometraje': 44999, 'detalles': [{'servicioId': 1, 'cantidad': 1}]}, asesor)
call('RN05 orden de otro dia', 'PATCH', '/ordenes/1/anular', 409, {'motivo': 'Cliente solicita cancelar'}, jefe)
call('RN05 ya anulada', 'PATCH', '/ordenes/4/anular', 409, {'motivo': 'Cliente solicita cancelar'}, jefe)
report, _ = call('Reporte agregado excluye anuladas', 'GET',
    '/reportes/ingresos-por-servicio?desde=2026-10-29&hasta=2026-10-29', 200, token=jefe)
assert sum(float(row['importeTotal']) for row in report) == 560
assert not any(row['codigo'] == 'SRV-007' for row in report)
call('Filtro sin resultados', 'GET', '/ordenes?placa=XXX-000', 200, token=asesor)
call('Fechas invalidas', 'GET', '/ordenes?desde=2027-01-01&hasta=2026-01-01', 400, token=asesor)

# CRUD completo con datos temporales y sin cambiar las ocho semillas.
used = {s['codigo'] for s in servicios}
n = next(i for i in range(900, 1000) if f'SRV-{i:03}' not in used)
service_body = {'codigo': f'SRV-{n:03}', 'nombre': 'Servicio temporal API', 'precio': 25, 'duracionMin': 30, 'activo': True}
service, _ = call('Servicio crear', 'POST', '/servicios', 201, service_body, jefe)
try:
    service_body['precio'] = 30
    call('Servicio actualizar', 'PUT', '/servicios/' + str(service['id']), 200, service_body, jefe)
    call('Servicio baja logica', 'PATCH', '/servicios/' + str(service['id']) + '/estado', 200, {'activo': False}, jefe)
finally:
    call('Servicio eliminar sin uso', 'DELETE', '/servicios/' + str(service['id']), 204, token=jefe)
vehicle_body = {'placa': f'TST-{n:03}', 'marca': 'Toyota', 'modelo': 'Prueba', 'anio': 2020, 'clienteId': 1}
vehicle, _ = call('Vehiculo crear', 'POST', '/vehiculos', 201, vehicle_body, asesor)
try:
    vehicle_body['modelo'] = 'Prueba editada'
    call('Vehiculo actualizar', 'PUT', '/vehiculos/' + str(vehicle['id']), 200, vehicle_body, asesor)
finally:
    call('Vehiculo eliminar sin ordenes', 'DELETE', '/vehiculos/' + str(vehicle['id']), 204, token=asesor)
valid = {'vehiculoId': 8, 'kilometraje': 1000, 'detalles': [{'servicioId': 1, 'cantidad': 2}, {'servicioId': 2, 'cantidad': 1}], 'total': 0}
order, _ = call('Orden atomica calcula total ignora total del cliente', 'POST', '/ordenes', 201, valid, asesor)
assert float(order['total']) == 220 and len(order['detalles']) == 2
call('Detalle de orden', 'GET', '/ordenes/' + str(order['id']), 200, token=jefe)
call('Motivo obligatorio', 'PATCH', '/ordenes/' + str(order['id']) + '/anular', 400, {'motivo': 'corto'}, jefe)
cancelled, _ = call('Anulacion mismo dia', 'PATCH', '/ordenes/' + str(order['id']) + '/anular', 200,
    {'motivo': 'Finaliza la prueba automatica de API'}, jefe)
assert cancelled['estado'] == 'ANULADA'
call('Sesion asesor continua valida tras 403', 'GET', '/auth/me', 200, token=asesor)
_, cors = call('Preflight CORS', 'OPTIONS', '/ordenes', 200, headers={
    'Origin': 'http://localhost:4200', 'Access-Control-Request-Method': 'POST', 'Access-Control-Request-Headers': 'authorization,content-type'})
assert cors.get('Access-Control-Allow-Origin') == 'http://localhost:4200'

path = Path(os.environ.get('AUTOANDES_RESULTADOS', 'C:/PHARMAPROYECT/tmp/autoandes-api-resultados.json'))
path.write_text(json.dumps({'fecha': datetime.datetime.now().isoformat(), 'casos': results, 'ordenPruebaAnulada': order['numero']}, indent=2, ensure_ascii=False), encoding='utf-8')
print('TOTAL:', len(results), 'correctos')
