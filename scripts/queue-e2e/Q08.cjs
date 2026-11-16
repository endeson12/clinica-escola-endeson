const {test}=require('./harness.cjs');
test("Q08 Deep link de rascunho não revela dados ao aluno",async({browser,base,tag,admin,teacher,student,p,st,turma,discipline,enrollment,activity,delivery,actor,person,create,get,queue,grade,ready,page,assert})=>{
await grade(8,'SEGREDO-QA',false);const ui=await page(student,'entregas?atividade='+activity+'&situacao=RASCUNHO');assert.match(await ui.locator('#conteudo').innerText(),/Não foi possível carregar/);assert(!((await ui.locator('body').innerText()).includes('SEGREDO-QA')));assert.equal(await ui.locator('[data-action="grade"]').count(),0);
await ui.goto(base+'/portal.html#entregas?atividade='+activity+'&situacao=PENDENTE');await ready(ui);assert.equal(await ui.locator('tbody tr').count(),1);assert.equal(await ui.locator('option[value="RASCUNHO"]').count(),0);assert.equal((await queue(student,'PENDENTE'))[0].feedback,null);
}).catch(e=>{console.error(e);process.exitCode=1});
