const {test}=require('./harness.cjs');
test("Q05 Troca docente revoga fila sem invalidar sessão",async({browser,base,tag,admin,teacher,student,p,st,turma,discipline,enrollment,activity,delivery,actor,person,create,get,queue,grade,ready,page,assert})=>{
await grade(7,'Preservado',true);const replacement=await person('PROFESSOR');const r=await admin.request.put(base+'/api/academico/turmas/'+turma,{data:{disciplinaId:discipline,professorId:replacement.id,nome:tag,periodo:'2099.1',vagas:20}});assert.equal(r.status(),200);
assert.equal((await get(teacher,'contexto')).usuario.perfil,'PROFESSOR');assert.equal((await teacher.request.get(base+'/api/academico/entregas?atividadeId='+activity)).status(),403);
const deny=await teacher.request.put(base+'/api/academico/entregas/'+delivery+'/avaliacao',{data:{nota:1,feedback:'Proibido',publicada:false}});assert.equal(deny.status(),403);
assert.equal((await queue(replacement.context,'PUBLICADA'))[0].feedback,'Preservado');await grade(8,'Novo docente',true,replacement.context);assert.equal((await queue(student,'PUBLICADA'))[0].nota,8);
}).catch(e=>{console.error(e);process.exitCode=1});
