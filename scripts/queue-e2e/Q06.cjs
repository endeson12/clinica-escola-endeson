const {test}=require('./harness.cjs');
test("Q06 Cancelamento de matrícula bloqueia leitura e novo envio",async({browser,base,tag,admin,teacher,student,p,st,turma,discipline,enrollment,activity,delivery,actor,person,create,get,queue,grade,ready,page,assert})=>{
const r=await admin.request.post(base+'/api/academico/matriculas/'+enrollment+'/cancelar',{data:{}});assert.equal(r.status(),200);assert.equal((await get(student,'contexto')).usuario.perfil,'ALUNO');
assert.equal((await student.request.get(base+'/api/academico/entregas?atividadeId='+activity)).status(),403);assert.deepEqual(await get(student,'entregas'),[]);
const newActivity=await create(teacher,'atividades',{turmaId:turma,titulo:'Após cancelamento',instrucoes:'Responder',prazo:'2099-12-31T23:59:00Z'});
assert.equal((await student.request.post(base+'/api/academico/entregas',{data:{atividadeId:newActivity,conteudo:'Não gravar'}})).status(),403);assert.equal((await queue(teacher,'PENDENTE'))[0].conteudo,'Resposta QA original');assert.equal((await get(teacher,'entregas?atividadeId='+newActivity)).length,0);
}).catch(e=>{console.error(e);process.exitCode=1});
