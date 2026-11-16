const {test}=require('./harness.cjs');
test("Q02 CSRF inválido não grava avaliação",async({browser,base,tag,admin,teacher,student,p,st,turma,discipline,enrollment,activity,delivery,actor,person,create,get,queue,grade,ready,page,assert})=>{
for(const token of ['', 'invalido']) {const r=await teacher.request.put(base+'/api/academico/entregas/'+delivery+'/avaliacao',{headers:{'X-XSRF-TOKEN':token},data:{nota:10,feedback:'Não gravar',publicada:true}});assert.equal(r.status(),403);}
const rows=await queue(teacher,'PENDENTE');assert.equal(rows.length,1);assert.equal(rows[0].nota,null);assert.equal(rows[0].publicada,false);assert.equal((await get(student,'boletim')).length,0);
await grade(0,'Token correto',true);assert.equal((await queue(student,'PUBLICADA'))[0].feedback,'Token correto');
}).catch(e=>{console.error(e);process.exitCode=1});
