package controllers;

import akka.dispatch.MessageDispatcher;
import com.fasterxml.jackson.databind.JsonNode;
import dispatchers.AkkaDispatcher;
import models.CampoEntity;
import models.MensajeDeUsuarioDTO;
import models.PozoEntity;
import models.UsuarioEntity;
import play.libs.Json;
import play.mvc.*;

import java.util.concurrent.*;

import static play.libs.Json.toJson;

public class PozoController extends Controller{

    public CompletionStage<Result> getPozos() {
        MessageDispatcher jdbcDispatcher = AkkaDispatcher.jdbcDispatcher;

        return CompletableFuture.
                supplyAsync(
                        () -> {
                            return PozoEntity.FINDER.all();
                        }
                        ,jdbcDispatcher)
                .thenApply(
                        pozoEntities -> {
                            return ok(toJson(pozoEntities));
                        }
                );
    }
    public CompletionStage<Result> getPozosRegion() {
        MessageDispatcher jdbcDispatcher = AkkaDispatcher.jdbcDispatcher;

        return CompletableFuture.
                supplyAsync(
                        () -> {
                            return PozoEntity.FINDER.all();
                        }
                        ,jdbcDispatcher)
                .thenApply(
                        pozoEntities -> {
                            return ok(toJson(pozoEntities));
                        }
                );
    }

    public CompletionStage<Result> createPozo(){
        MessageDispatcher jdbcDispatcher = AkkaDispatcher.jdbcDispatcher;
        JsonNode nPozo = request().body().asJson();
        PozoEntity pozo = Json.fromJson( nPozo , PozoEntity.class ) ;
        return CompletableFuture.supplyAsync(
                ()->{
                    pozo.save();
                    return pozo;
                }
                ,jdbcDispatcher
        ).thenApply(
                pozoEntity -> {
                    return ok(Json.toJson(pozoEntity));
                }
        );
    }

    public CompletionStage<Result> updatePozo()
    {
        //debe llegar asi:
        //{ "user":"jg",
        //  "password" : "123",
        //  "type":"Jefe de Campo",
        //  "payload":"{"Status":"PRODUCCION" , "Id": 123}"}
        // o "PARADO"   si es "Jefe de Produccion" el status puede ser "ABIERTO" o "CLAUSURADO"

        MessageDispatcher jdbcDispatcher = AkkaDispatcher.jdbcDispatcher;
        //llega el json con el username, password, type, payload
        JsonNode nMessage = request().body().asJson();
        MensajeDeUsuarioDTO mensajeCompleto  = Json.fromJson( nMessage, MensajeDeUsuarioDTO.class ) ;

        JsonNode payload = Json.parse(mensajeCompleto.getPayload());
        System.out.println(mensajeCompleto.getPayload());
        System.out.println(payload);
        PozoEntity nuevoPozo = Json.fromJson(payload,PozoEntity.class);

        PozoEntity.Estado nuevoStatus = nuevoPozo.getEstado();
        Long idPozo = nuevoPozo.getId();

        //usuario.setPassword(mensajeCompleto.getPassword());
        //usuario.setUsername(mensajeCompleto.getUser());
        //usuario.setType(usuario.getTipoUsuario(mensajeCompleto.getType()) );

        return CompletableFuture.supplyAsync(
                ()->{
                    PozoEntity elNull = null;
                    //verifica el campo del jefe
                    if (mensajeCompleto.getUser()!=null)
                    {
                        System.out.println(1);
                        UsuarioEntity usuario = UsuarioEntity.FINDER.byId(mensajeCompleto.getUser());//.eq("password",mensajeCompleto.getPassword())
                        if(usuario != null && usuario.getType() == ( usuario.getTipoUsuario(mensajeCompleto.getType()))) {
                            System.out.println(2);
                            //si es jefe
                            //si es jefe de produccion no se le aplican mas filtros
                            if(usuario.getType() == UsuarioEntity.TipoUsuario.JEFE_PRODUCCION)
                            {
                                System.out.println(3);
                                PozoEntity pozoBuscado = PozoEntity.FINDER.byId(idPozo);
                                if(pozoBuscado != null)
                                {
                                    System.out.println(4);
                                    System.out.println("El JEFE DE PRODUCCION esta actualizando el pozo: "+ pozoBuscado.getId() + "  al estado: "+ (nuevoStatus));
                                    pozoBuscado.setEstado(pozoBuscado.getEstado());
                                    pozoBuscado.update();
                                    return pozoBuscado;
                                }
                                else
                                {
                                    System.out.println(5);
                                    return elNull;
                                }

                            }
                            else
                            {
                                System.out.println(6);
                                CampoEntity campo = CampoEntity.FINDER.where().eq("idJefeCampo.username", usuario.getUsername()).setMaxRows(1).findUnique();
                                //si si es jefe de campo enotnces no sera null
                                if(campo != null)
                                {
                                    System.out.println(7);
                                    System.out.println("id campo :"+campo.getId());
                                    System.out.println("id pozo :"+idPozo);
                                    PozoEntity pozoBuscado = PozoEntity.FINDER.where().conjunction().eq("campo.id", campo.getId()).eq("id", idPozo).setMaxRows(1).findUnique();
                                    //si existe el pozo es que ese jefe de campo si tenia entre sus campos el pozo
                                    if(pozoBuscado != null)
                                    {
                                        System.out.println(8);
                                        System.out.println("El JEFE DE CAMPO esta actualizando el pozo: "+ pozoBuscado.getId() + "  al estado: "+ pozoBuscado.getEstado());
                                        pozoBuscado.setEstado(nuevoStatus);
                                        pozoBuscado.update();
                                        return pozoBuscado;
                                    }
                                    else
                                    {
                                        System.out.println(9);
                                        return elNull;
                                    }

                                }
                                else
                                {
                                    System.out.println(10);
                                    return elNull;
                                }


                            }

                        }
                        else
                        {
                            System.out.println(11);
                                // mal usuario
                            return elNull;
                        }
                    }
                    else
                    {

                        System.out.println(12);
                        return elNull;
                    }


                }
                ,jdbcDispatcher
        ).thenApply(
                pozoEntity -> {
                    if(pozoEntity == null)
                    {
                        return forbidden("No eres un usuario valido");
                    }
                    return ok(Json.toJson(pozoEntity));
                }


        );
    }


    public CompletionStage<Result> getPozo(Long id){
        MessageDispatcher jdbcDispatcher = AkkaDispatcher.jdbcDispatcher;
        return CompletableFuture.supplyAsync(
                ()->{
                    return PozoEntity.FINDER.byId(id);
                }
                ,jdbcDispatcher
        ).thenApply(
                productEntity -> {
                    return ok(Json.toJson(productEntity));
                }
        );
    }

    public CompletionStage<Result> deletePozo(Long id){
        MessageDispatcher jdbcDispatcher = AkkaDispatcher.jdbcDispatcher;
        return CompletableFuture.supplyAsync(
                ()->{
                    PozoEntity.FINDER.byId(id).delete();
                    return "Pozo con id "+id+" eliminado";
                }
                ,jdbcDispatcher
        ).thenApply(
                productEntity -> {
                    return ok(Json.toJson(productEntity));
                }
        );
    }
}
