const {test}=require('./harness.cjs');
test("Q04 Colegas da mesma turma mantêm entregas separadas",async({browser,base,tag,admin,teacher,student,p,st,turma,discipline,enrollment,activity,delivery,actor,person,create,get,queue,grade,ready,page,assert})=>{
const peer=await person('ALUNO');await create(admin,'matriculas',{turmaId:turma,alunoId:peer.id});const e=await create(peer.context,'entregas',{atividadeId:activity,conteudo:'Resposta do colega'});await grade(5,'Colega',true,teacher,e);await grade(9,'Próprio',true);
const mine=await queue(student,'PUBLICADA'),theirs=await queue(peer.context,'PUBLICADA');assert.deepEqual(mine.map(r=>r.id),[delivery]);assert.deepEqual(theirs.map(r=>r.id),[e]);assert.equal(mine[0].feedback,'Próprio');
const forbidden=await student.request.put(base+'/api/academico/entregas/'+e+'/avaliacao',{data:{nota:10,feedback:'Intrusão',publicada:true}});assert.equal(forbidden.status(),403);
const ui=await page(student,'entregas?atividade='+activity);assert.equal(await ui.locator('tbody tr').count(),1);assert(!((await ui.locator('#table-results').innerText()).includes('Resposta do colega')));assert.equal((await queue(teacher,'PUBLICADA')).length,2);
}).catch(e=>{console.error(e);process.exitCode=1});
