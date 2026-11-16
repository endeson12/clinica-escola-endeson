const {test}=require('./harness.cjs');
test("Q03 Retirada e republicação na mesma sessão do aluno",async({browser,base,tag,admin,teacher,student,p,st,turma,discipline,enrollment,activity,delivery,actor,person,create,get,queue,grade,ready,page,assert})=>{
await grade(8,'Primeira publicação',true);const ui=await page(student,'entregas?atividade='+activity+'&situacao=PUBLICADA');assert.equal(await ui.locator('tbody tr').count(),1);
await grade(8,'Feedback oculto',false);await ui.reload();await ready(ui);assert.equal(await ui.locator('tbody tr').count(),0);assert.equal((await get(student,'boletim')).length,0);
const pending=await queue(student,'PENDENTE');assert.equal(pending[0].nota,null);assert.equal(pending[0].feedback,null);assert.equal(pending[0].conteudo,'Resposta QA original');
await grade(9,'Republicada',true);await ui.reload();await ready(ui);assert.equal(await ui.locator('tbody tr').count(),1);assert.equal((await queue(student,'PUBLICADA'))[0].feedback,'Republicada');
}).catch(e=>{console.error(e);process.exitCode=1});
